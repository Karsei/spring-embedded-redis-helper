package kr.pe.karsei.helper.embeddedredis;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class EmbeddedRedis implements InitializingBean, DisposableBean {
    private final ReentrantLock lock = new ReentrantLock();
    private static RedisServer redisServer;

    @Getter
    private static int port;
    @Getter
    private final String password;

    public EmbeddedRedis(String password) throws IOException {
        this.password = password;
        if (redisServer == null) {
        lock.lock();
            try {
                if (redisServer == null) {
                    // 테스트 실행 시 포트 충돌 막기 위함
                    this.port = findAvailablePort();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public EmbeddedRedis(int port, String password) {
        this.password = password;
        if (redisServer == null) {
            this.port = port;
        }
    }

    @Override
    public void afterPropertiesSet() {
        // 이미 할당받은 적이 있다면 건너뜀
        if (redisServer != null) {
            log.info("Embedded Redis - Already server started, starting skipped.");
            return;
        }

        lock.lock();
        try {
            RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider()
                    // https://github.com/tporadowski/redis
                    .override(OS.WINDOWS, "binary/redis/redis-server-5.0.14-x64.exe")
                    // openjdk image 상에서 apk add --update redis 실행 후 바이너리 복사
                    .override(OS.UNIX, "binary/redis/redis-server-6.2.12-unix")
                    // https://download.redis.io/releases/
                    // 직접 빌드 후 바이너리 복사
                    .override(OS.MAC_OS_X, isAppleSilicon()
                            ? "binary/redis/redis-server-6.2.14-mac-arm"
                            : "binary/redis/redis-server-6.2.14-mac-intel");

            redisServer = RedisServer.builder()
                    .redisExecProvider(redisExecProvider)
                    .port(port)
                    .setting("maxmemory 128M")
                    .setting("requirepass " + this.password)
                    .build();
            log.info("Embedded Redis - Trying start redis on port {}...", port);

            redisServer.start();
            log.info("Embedded Redis - Started on port {}.", port);
        } catch (Exception e) {
            redisServer.stop();
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void destroy() {
        if (redisServer != null && redisServer.isActive()) {
            lock.lock();
            try {
                log.info("Embedded Redis - Shutdown initiated... (port: {})", port);
                redisServer.stop();
                log.info("Embedded Redis - Shutdown completed. (port: {})", port);
            } finally {
                lock.unlock();
            }
        }
    }

    private int findAvailablePort() throws IOException {
        int max = 65535;
        int min = 10000;
        int pos = 0;
        while (pos < (max - min)) {
            Random random = new Random();
            int port = random.ints(10000, 65535)
                    .findFirst()
                    .getAsInt();

            Process process = null;
            boolean isRunning;
            try {
                process = executeGrepProcessCommand(port);
                isRunning = isRunning(process);
            } catch (Exception e) {
                if (null != process)
                    process.destroy();
                throw e;
            }

            if (!isRunning) {
                return port;
            }
            pos++;
        }

        throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
    }

    private Process executeGrepProcessCommand(int port) throws IOException {
        String OS = getOs();
        if (OS.contains("win")) {
            log.info("OS is " + OS + " " + port);
            String command = String.format("netstat -nao | find \"LISTEN\" | find \"%d\"", port);
            String[] shell = {"cmd.exe", "/y", "/c", command};
            return Runtime.getRuntime().exec(shell);
        }

        String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }

    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }
        } catch (Exception ignored) {}

        return StringUtils.hasText(pidInfo.toString());
    }

    private boolean isAppleSilicon() {
        String OS = getOs();
        if (OS.contains("mac")) {
            String arch = System.getProperty("os.arch").toLowerCase();
            return arch.contains("aarch64");
        }
        return false;
    }

    private String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }
}

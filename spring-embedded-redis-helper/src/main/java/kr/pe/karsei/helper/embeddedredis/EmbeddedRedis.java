package kr.pe.karsei.helper.embeddedredis;

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

@Slf4j
public class EmbeddedRedis implements InitializingBean, DisposableBean {
    private static RedisServer redisServer;
    private static int port;

    private final String password;

    public EmbeddedRedis(String password) throws IOException {
        this.password = password;

        if (redisServer == null) {
            // 테스트 실행 시 포트 충돌 막기 위함
            port = findAvailablePort();
        }
    }

    @Override
    public void afterPropertiesSet() {
        // 이미 할당받은 적이 있다면 건너뜀
        if (redisServer != null) {
            log.info("Embedded Redis - Already server started, starting skipped.");
            return;
        }

        RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider()
                // https://github.com/redis-windows/redis-windows
                .override(OS.WINDOWS, "binary/redis/redis-server-3.0.504-x64.exe")
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
        try {
            redisServer.start();
            log.info("Embedded Redis - Started on port {}.", port);
        } catch (Exception e) {
            redisServer.stop();
            throw e;
        }
    }

    @Override
    public void destroy() {
        if (redisServer != null) {
            log.info("Embedded Redis - Shutdown initiated... (port: {})", port);
            redisServer.stop();
            log.info("Embedded Redis - Shutdown completed. (port: {})", port);
        }
    }

    public int getPort() {
        return port;
    }

    private int findAvailablePort() throws IOException {
        for (int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            boolean isRunning = isRunning(process);
            process.destroy();
            if (!isRunning) {
                return port;
            }
        }

        throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
    }

    private Process executeGrepProcessCommand(int port) throws IOException {
        String OS = getOs();
        if (OS.contains("win")) {
            log.info("OS is  " + OS + " " + port);
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

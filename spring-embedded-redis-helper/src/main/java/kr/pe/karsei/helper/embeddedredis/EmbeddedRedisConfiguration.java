package kr.pe.karsei.helper.embeddedredis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class EmbeddedRedisConfiguration implements InitializingBean, DisposableBean {
    /**
     * 포트
     * <p>{@link org.springframework.boot.autoconfigure.data.redis.RedisProperties} 를 쓰지 않는 이유는 prefix 값이 javax(spring.redis), jakarta(spring.data.redis) 에 따라 서로 다르기 때문</p>
     */
    @Value("#{'${spring.redis.port:${spring.data.redis.port:0}}'}")
    private int port;

    /**
     * 비밀번호
     * <p>{@link org.springframework.boot.autoconfigure.data.redis.RedisProperties} 를 쓰지 않는 이유는 prefix 값이 javax(spring.redis), jakarta(spring.data.redis) 에 따라 서로 다르기 때문</p>
     */
    @Value("#{'${spring.redis.password:${spring.data.redis.password:}}'}")
    private String password;

    private RedisServer redisServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 값 확인
        if (0 == port)
            throw new IllegalArgumentException("Redis Port 가 존재하지 않습니다.");
        if (!StringUtils.hasText(password))
            throw new IllegalArgumentException("Redis Password 가 존재하지 않습니다.");

        // 테스트 실행 시 포트 충돌 막기 위함
        int port = isRedisRunning() ? findAvailablePort() : this.port;

        RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider()
                // https://github.com/redis-windows/redis-windows
                .override(OS.WINDOWS, "binary/redis/redis-server-6.2.14-x64.exe")
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
                .setting("maxmemory 64M")
                .setting("requirepass " + this.password)
                .build();
        redisServer.start();
    }

    @Override
    public void destroy() {
        if (redisServer != null)
            redisServer.stop();
    }

    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(this.port));
    }

    public int findAvailablePort() throws IOException {
        for (int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
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

    static boolean isAppleSilicon() {
        String OS = getOs();
        if (OS.contains("mac")) {
            String arch = System.getProperty("os.arch").toLowerCase();
            return arch.contains("aarch64");
        }
        return false;
    }

    static String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }
}
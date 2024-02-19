package kr.pe.karsei.embeddedredisdemo.config.redis;

import kr.pe.karsei.helper.embeddedredis.EmbeddedRedis;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EmbeddedRedisConfiguration {
    @Bean
    public EmbeddedRedis embeddedRedis(RedisProperties redisProperties) {
        return new EmbeddedRedis(redisProperties.getPort(), redisProperties.getPassword());
    }
}

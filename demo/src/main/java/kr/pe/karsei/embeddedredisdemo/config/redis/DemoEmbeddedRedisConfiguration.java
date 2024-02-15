package kr.pe.karsei.embeddedredisdemo.config.redis;

import kr.pe.karsei.helper.embeddedredis.EmbeddedRedisConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile({"default", "test"})
@Configuration(proxyBeanMethods = false)
@Import(EmbeddedRedisConfiguration.class)
public class DemoEmbeddedRedisConfiguration {
}

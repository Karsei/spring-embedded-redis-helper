package kr.pe.karsei.helper.embeddedredis;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.test.context.ContextCustomizer;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmbeddedRedisContextCustomizerTest {
    @Test
    void customizeContext() {
        ContextCustomizer customizer = new EmbeddedRedisContextCustomizer();

        ConfigurableApplicationContext context = new GenericApplicationContext();
        Properties properties = new Properties();
        properties.put("spring.data.redis.password", "1234");
        context.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("testContextProps", properties));
        customizer.customizeContext(context, null);
        try {
            context.refresh();

            EmbeddedRedis bean = context.getBean(EmbeddedRedis.class);
            assertThat(bean.getPort()).isEqualTo(10000);
        } finally {
            context.close();
        }
    }

    @Test
    void customizeContextWithPasswordError() {
        ContextCustomizer customizer = new EmbeddedRedisContextCustomizer();

        ConfigurableApplicationContext context = new GenericApplicationContext();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customizer.customizeContext(context, null));
        assertThat(exception)
                .isNotNull()
                .hasMessage("Redis Password 가 존재하지 않습니다.");
    }
}
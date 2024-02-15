package kr.pe.karsei.helper.embeddedredis;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class EmbeddedRedisConfigurationTest {
    @Test
    void testAppleSilicon() {
        boolean result = GabiaEmbeddedRedisConfiguration.isAppleSilicon();
        log.info(String.valueOf(result));
    }

    @Test
    void testOs() {
        String os = GabiaEmbeddedRedisConfiguration.getOs();
        log.info(os);
    }
}
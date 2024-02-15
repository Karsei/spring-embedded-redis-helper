package kr.pe.karsei.embeddedredisdemo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class TestRedisServiceTest {
    @Autowired
    private TestRedisService service;

    @Test
    void testRedis() {
        Boolean result = Assertions.assertDoesNotThrow(() -> service.insert("test", "hello, world!"));
        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }
}
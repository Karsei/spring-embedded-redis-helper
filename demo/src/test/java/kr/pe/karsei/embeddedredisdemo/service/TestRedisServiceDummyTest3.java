package kr.pe.karsei.embeddedredisdemo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TestRedisServiceDummyTest3 {
    @Autowired
    private TestRedisService service;

    @Test
    void testRedis1() {
        Boolean result = Assertions.assertDoesNotThrow(() -> service.insert("test44", "hello, world!"));
        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }

    @Test
    void testRedis2() {
        Boolean result = Assertions.assertDoesNotThrow(() -> service.insert("test55", "hello, world!"));
        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }

    @Test
    void testRedis3() {
        Boolean result = Assertions.assertDoesNotThrow(() -> service.insert("test66", "hello, world!"));
        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }
}
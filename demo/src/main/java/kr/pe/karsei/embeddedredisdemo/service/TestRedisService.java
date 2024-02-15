package kr.pe.karsei.embeddedredisdemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestRedisService {
    private final RedisTemplate<String, String> testRedisTemplate;

    public Boolean insert(String key, String value) {
        ValueOperations<String, String> ops = testRedisTemplate.opsForValue();
        return ops.setIfAbsent(key, value);
    }
}

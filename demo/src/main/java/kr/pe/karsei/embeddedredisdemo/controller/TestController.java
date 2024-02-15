package kr.pe.karsei.embeddedredisdemo.controller;

import kr.pe.karsei.embeddedredisdemo.service.TestRedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "EmbeddedRedisDemo", description = "Embedded Redis 데모")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {
    private final TestRedisService service;

    @Operation(summary = "Redis 사용 테스트", description = "Redis 에 접속하여 키를 생성합니다.")
    @GetMapping("test")
    public String test() {
        service.insert("test", "Hello, world!");
        return "good";
    }
}

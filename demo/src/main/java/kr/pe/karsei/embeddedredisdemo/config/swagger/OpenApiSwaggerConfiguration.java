package kr.pe.karsei.embeddedredisdemo.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;

public class OpenApiSwaggerConfiguration {
    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info()
                        .title("Embedded Redis 데모")
                        .description("Embedded Redis 가 정상적으로 잘 작동되는지 확인합니다.")
                        .version("1.0.0"))
                .addServersItem(new Server().url("/"));
        return openAPI;
    }
}

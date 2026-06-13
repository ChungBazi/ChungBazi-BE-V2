package com.chungbazi.server.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "👖 청바지 API 명세서",
                description = "👖 청바지API 명세서",
                version = "v1.0.0"
        ),
        servers = {
                @Server(
                        url = "${springdoc.local-server-url}",
                        description = "Local Server URL"
                ),
                @Server(
                        url = "${springdoc.dev-server-url}",
                        description = "Develop Server URL"
                ),
                @Server(
                        url = "${springdoc.prod-server-url}",
                        description = "Production Server URL"
                )
        }
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }
}

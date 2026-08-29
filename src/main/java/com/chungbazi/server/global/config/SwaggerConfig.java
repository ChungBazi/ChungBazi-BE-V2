package com.chungbazi.server.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

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
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("BearerAuth", securityScheme))
                .security(Collections.singletonList(securityRequirement));
    }
}

package com.example.dm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "dm-scheduler API",
                version = "1.0",
                description = "dm-scheduler OpenAPI"
        )
)
public class OpenApiConfig {
}


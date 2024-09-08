package com.project.api.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "AUTHORIZATION",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "Bearer",
)
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Base API")
                    .version("1.0"),
            ).addSecurityItem(SecurityRequirement().addList("AUTHORIZATION"))
}

package com.project.api.config

import com.nimbusds.jose.JWSAlgorithm
import com.project.api.commons.security.JwtAccessDeniedHandler
import com.project.api.commons.security.JwtAuthenticationEntryPoint
import com.project.api.config.properties.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val properties: SecurityProperties,
) {
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        val accessDeniedHandler = JwtAccessDeniedHandler()
        val authenticationEntryPoint = JwtAuthenticationEntryPoint()

        return httpSecurity
            .csrf { it.disable() }
            .cors {
                it.configurationSource {
                    return@configurationSource CorsConfiguration()
                        .apply {
                            // TODO : base url yml에 추가
                            setAllowedOriginPatterns(listOf())
                            allowCredentials = true
                            addAllowedHeader("*")
                            addAllowedMethod("*")
                        }
                }
            }.httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.accessDeniedHandler(accessDeniedHandler)
                it.authenticationEntryPoint(authenticationEntryPoint)
            }.authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/actuator/**",
                        "/swagger-ui*/**",
                        "/v3/api-docs/**",
                        "/error",
                        "/h2-console/**",
                    ).permitAll()
                it.requestMatchers(HttpMethod.POST, "/v1/users/create", "/v1/users/login").permitAll()
                it.anyRequest().permitAll()
            }.oauth2ResourceServer {
                it.accessDeniedHandler(accessDeniedHandler)
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.jwt { jwtConfigurer ->
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtGrantedAuthoritiesConverter()
        /*converter.setAuthoritiesClaimName("roles")
        converter.setAuthorityPrefix("ROLE_")*/

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter)
        return jwtAuthenticationConverter
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(properties.key.toByteArray(), JWSAlgorithm.HS256.toString())
        return NimbusJwtDecoder.withSecretKey(key).build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}

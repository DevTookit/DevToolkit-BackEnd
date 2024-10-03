package com.project.api.config

import com.project.api.config.properties.UrlProperties
import com.project.api.service.api.DiscordApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class RestClientConfig(
    private val urlProperties: UrlProperties,
) {
    private inline fun <reified T> createApiClient(
        baseUrl: String? = null,
        noinline headersConfigurer: (HttpHeaders) -> Unit = {},
    ): T {
        val restClient =
            RestClient
                .builder()
                .defaultHeaders(headersConfigurer)

        baseUrl?.let { restClient.baseUrl(it) }

        val factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient.build())).build()
        return factory.createClient(T::class.java)
    }

    @Bean
    fun krxApi(): DiscordApi = createApiClient(urlProperties.discord)
}

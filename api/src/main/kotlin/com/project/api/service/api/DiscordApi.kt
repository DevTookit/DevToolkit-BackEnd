package com.project.api.service.api

import com.project.api.service.api.dto.DiscordRequest
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
interface DiscordApi {
    @PostExchange
    fun sendMessage(
        @RequestBody request: DiscordRequest,
    )
}

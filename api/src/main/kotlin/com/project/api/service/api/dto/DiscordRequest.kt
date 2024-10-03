package com.project.api.service.api.dto

import org.springframework.http.HttpStatusCode
import java.time.LocalDateTime

data class DiscordRequest(
    val content: String = "# \uD83D\uDEA8 에러 발생 비이이이이사아아아앙",
    val embeds: List<Embed>,
)

data class Embed(
    val description: String,
) {
    companion object {
        fun createMessage(
            date: LocalDateTime,
            path: String,
            error: String,
            status: HttpStatusCode,
        ): Embed =
            Embed(
                description =
                    " \n" +
                        "### \uD83D\uDD56 발생시간 \n" +
                        "$date \n" +
                        "### \uD83D\uDD17 요청 URL \n" +
                        "$path \n" +
                        "### \uD83D\uDCAB 상태 \n" +
                        "$status \n" +
                        "### \uD83D\uDCC4 상세 에러 \n" +
                        "```\n" +
                        "${error.substring(0,Math.min(error.length, 10000))}" +
                        "\n```",
            )
    }
}

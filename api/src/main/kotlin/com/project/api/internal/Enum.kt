package com.project.api.internal

enum class ErrorMessage(
    val message: String,
) {
    INVALID_ENTITY("처리불가능한 엔티티입니다."),
    NOT_MATCH_PASSWORD("비밀번호가 틀렸습니다."),
    NOT_FOUND_USER("해당 유저를 찾을 수 없습니다."),
}

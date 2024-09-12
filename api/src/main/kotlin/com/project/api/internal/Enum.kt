package com.project.api.internal

enum class ErrorMessage(
    val message: String,
) {
    INVALID_ENTITY("처리불가능한 엔티티입니다."),
    NOT_MATCH_PASSWORD("비밀번호가 틀렸습니다."),
    NOT_FOUND_USER("해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_GROUP("해당 그룹을 찾을 수 없습니다."),
    NOT_FOUND_GROUP_USER("해당 그룹 회원을 찾을 수 없습니다."),
    NEW_PASSWORD_MATCH_OLD_PASSWORD("새로운 패스워드가 이전 패스워드와 일치합니다."),
    IMPOSSIBLE_LOGIN("로그인이 불가능합니다. 관리자에게 문의해주세요"),
    UNAUTHORIZED("해당권한으로 불가능한 요청입니다."),
    CONFLICT_ENTITY("이미 존재하는 리소스 입니다."),
}

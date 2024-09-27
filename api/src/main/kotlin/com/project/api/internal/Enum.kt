package com.project.api.internal

enum class ErrorMessage(
    val message: String,
) {
    INVALID_ENTITY("처리불가능한 엔티티입니다."),
    NOT_FOUND("해당 엔티티를 찾을 수 없습니다."),
    NOT_MATCH_PASSWORD("비밀번호가 틀렸습니다."),
    NOT_FOUND_USER("해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_GROUP("해당 그룹을 찾을 수 없습니다."),
    NOT_FOUND_GROUP_USER("해당 그룹 회원을 찾을 수 없습니다."),
    NOT_FOUND_CATEGORY("해당 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_NOTIFICATION("해당 알림을 찾을 수 없습니다."),
    NOT_FOUND_SECTION("해당 섹션을 찾을 수 없습니다."),
    NOT_FOUND_CONTENT("해당 컨텐츠를 찾을 수 없습니다"),
    NOT_FOUND_BOOKMARK("해당 북마크를 찾을 수 없습니다"),
    NOT_FOUND_FOLDER("해당 폴더를 찾을 수 없습니다."),
    NOT_FOUND_FOLDER_FILE("해당 폴더 파일를 찾을 수 없습니다."),
    NEW_PASSWORD_MATCH_OLD_PASSWORD("새로운 패스워드가 이전 패스워드와 일치합니다."),
    IMPOSSIBLE_CREATE_CONTENT("해당 카테고리는 컨텐츠 생성이 불가능합니다"),
    IMPOSSIBLE_LOGIN("로그인이 불가능합니다. 관리자에게 문의해주세요"),
    IMPOSSIBLE_PASSWORD("이전 비밀번호와 다른 비밀번호로 설정해주세요"),
    IMPOSSIBLE_NOTIFICATION("최상위 카테고리만 알림설정이 가능합니다"),
    UNAUTHORIZED("해당권한으로 불가능한 요청입니다."),
    CONFLICT_ENTITY("이미 존재하는 리소스 입니다."),
}

enum class EmailForm(
    val subject: String,
    val message: String,
) {
    VERIFY_EMAIL("[DevToolKit] 본인인증 코드", "고객님의 본인인증 코드는 다음과 같습니다."),
}

enum class FilePath {
    PROFILE,
    CONTENT,
}

enum class SortType {
    NEW,
    NAME,
    DEFAULT,
}

enum class FolderReadType {
    FILE,
    FOLDER,
    DEFAULT,
}

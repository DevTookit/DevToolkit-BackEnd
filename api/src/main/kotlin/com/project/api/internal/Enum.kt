package com.project.api.internal

enum class ErrorMessage(
    val message: String,
) {
    INVALID_ENTITY("처리불가능한 엔티티입니다."),
    NULL("NullPointException 관련 에러입니다."),
    NOT_MATCH_PASSWORD("비밀번호가 틀렸습니다."),
    NOT_MATCH_PASSWORD_MAX_REACHED("비밀번호 5회이상 틀렸습니다. 이메일 인증을 통해 비밀번호재설정 해주세요."),
    NOT_FOUND_USER("해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_GROUP("해당 그룹을 찾을 수 없습니다."),
    NOT_EXIST_CODE("인증코드와 존재하지 않습니다"),
    NOT_FOUND_GROUP_USER("해당 그룹 회원을 찾을 수 없습니다."),
    NOT_FOUND_CATEGORY("해당 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_NOTIFICATION("해당 알림을 찾을 수 없습니다."),
    NOT_FOUND_SECTION("해당 섹션을 찾을 수 없습니다."),
    NOT_FOUND_CONTENT("해당 컨텐츠를 찾을 수 없습니다"),
    NOT_FOUND_BOOKMARK("해당 북마크를 찾을 수 없습니다"),
    NOT_FOUND_FOLDER("해당 폴더를 찾을 수 없습니다."),
    NOT_FOUND_FOLDER_FILE("해당 폴더 파일를 찾을 수 없습니다."),
    NOT_FOUND_ANNOUNCE("해당 공지사항을 찾을 수 없습니다."),
    NEW_PASSWORD_MATCH_OLD_PASSWORD("새로운 패스워드가 이전 패스워드와 일치합니다."),
    IMPOSSIBLE_CREATE_CONTENT("해당 카테고리는 컨텐츠 생성이 불가능합니다"),
    INCORRECT_REFRESH_TOKEN("리프레쉬 토큰이 옳지 않습니다."),
    CONTACT_ADMIN("로그인이 불가능합니다. 관리자에게 문의해주세요"),
    NOT_EMAIL_VERIFIED("이메일 인증을 진행해주세요"),
    IMPOSSIBLE_PASSWORD("이전 비밀번호와 다른 비밀번호로 설정해주세요"),
    IMPOSSIBLE_NOTIFICATION("최상위 카테고리만 알림설정이 가능합니다"),
    UNAUTHORIZED("해당권한으로 불가능한 요청입니다."),
    ALREADY_VERIFIED_EMAIL("이미 검증한 이메일입니다."),
    CONFLICT_ENTITY("이미 존재하는 리소스 입니다."),
    FORBIDDEN("권한이 없습니다"),
    GROUP_OWNER_CANNOT_LEAVE("그룹의 소유자는 탈퇴할 수 없습니다."),
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

enum class RedisType(
    val expiredTime: Long? = null,
) {
    VISIT_CONTENT,
    VISIT_GROUP,
    HOT_GROUP(600),
    JOIN_GROUP,
    HOT_CONTENT,
}

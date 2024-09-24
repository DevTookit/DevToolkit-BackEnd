package com.project.core.internal

enum class GroupRole {
    TOP_MANAGER,
    MANAGER,
    USER,
    PENDING, // 관리자의 승인을 대기 중인 회원
    INVITED, // 관리자, 부관리자에 의해 초대된 회원
    SUSPENDED, // 활동 정지 회원
    ;

    fun isActive(): Boolean =
        when (this) {
            PENDING, SUSPENDED, INVITED -> false
            else -> true
        }

    fun isAdmin(): Boolean =
        when (this) {
            TOP_MANAGER, MANAGER -> true
            else -> false
        }

    fun isTopAmin(): Boolean =
        when (this) {
            TOP_MANAGER -> true
            else -> false
        }
}

enum class CategoryNotificationType {
    ALL,
    MENTIONS,
    NONE,
}

enum class NotificationType {
    CONTENT,
    MENTION,
    INVITATION,
    NOTICE,
}

enum class SectionType {
    MENU,
    REPOSITORY,
}

enum class ContentType {
    CODE,
    BOARD,
    FILE,
}

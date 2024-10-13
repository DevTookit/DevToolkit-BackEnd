package com.project.api.web.dto.response

import com.project.api.web.dto.response.ContentFolderDetailResponse.Companion.toContentFolderDetailResponse
import com.project.core.domain.content.Content
import com.project.core.internal.ContentType

data class ContentFolderResponse(
    val parentId: Long?,
    val createdDate: Long?,
    val lastModifiedDate: Long?,
    val name: String,
    val writerName: String,
    val writerId: Long?,
    val type: ContentType,
    val contents: List<ContentFolderDetailResponse>,
) {
    companion object {
        fun Content.toContentFolderResponse(contents: List<Content>) =
            ContentFolderResponse(
                parentId = this.id,
                createdDate = this.createdDate,
                lastModifiedDate = this.lastModifiedDate,
                name = name,
                writerId = this.groupUser.id,
                writerName = this.groupUser.name,
                type = this.type,
                contents =
                    contents.map {
                        it.toContentFolderDetailResponse()
                    },
            )
    }
}

data class ContentFolderDetailResponse(
    val contentId: Long?,
    val createdDate: Long?,
    val lastModifiedDate: Long?,
    val name: String,
    val writerName: String,
    val writerId: Long?,
    val type: ContentType,
    val url: String?,
    val sectionId: Long?,
) {
    companion object {
        fun Content.toContentFolderDetailResponse() =
            ContentFolderDetailResponse(
                contentId = this.id,
                createdDate = this.createdDate,
                lastModifiedDate = this.lastModifiedDate,
                name = this.name,
                writerId = this.groupUser.id,
                writerName = this.groupUser.name,
                type = this.type,
                url = this.url,
                sectionId = this.section.id,
            )
    }
}

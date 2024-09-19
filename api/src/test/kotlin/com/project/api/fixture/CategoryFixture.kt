package com.project.api.fixture

import com.project.api.repository.category.CategoryNotificationRepository
import com.project.api.repository.category.CategoryRepository
import com.project.core.domain.category.Category
import com.project.core.domain.group.Group
import org.springframework.stereotype.Component
import java.util.Random
import java.util.UUID

@Component
class CategoryFixture(
    private val categoryRepository: CategoryRepository,
    private val categoryNotificationRepository: CategoryNotificationRepository,
) : Fixture {

    fun create(
        name: String = UUID.randomUUID().toString(),
        isPublic: Boolean = Random().nextBoolean(),
        group: Group,
    ) = categoryRepository.save(Category(name, isPublic, group))

    override fun tearDown() {
        categoryNotificationRepository.deleteAll()
        categoryRepository.deleteAll()
    }
}

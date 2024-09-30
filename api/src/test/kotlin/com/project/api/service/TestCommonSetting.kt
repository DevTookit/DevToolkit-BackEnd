package com.project.api.service

import com.project.api.supprot.container.RedisTestContainer
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ExtendWith(RedisTestContainer::class)
@ActiveProfiles("test")
abstract class TestCommonSetting {
}
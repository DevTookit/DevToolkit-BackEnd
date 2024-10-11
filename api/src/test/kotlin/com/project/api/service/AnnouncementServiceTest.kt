package com.project.api.service

import com.project.api.repository.announcement.AnnouncementRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired

class AnnouncementServiceTest(
    @Autowired private val announcementService: AnnouncementService,
    @Autowired private val announcementRepository: AnnouncementRepository,
) : TestCommonSetting() {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
}
package com.project.api.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {
    fun add(
        key: String,
        value: String,
        expiredTime: Long,
    ) {
        val isNewKey = redisTemplate.opsForValue().get(key)
        if (isNewKey == null) {
            redisTemplate.expire(key, expiredTime, TimeUnit.SECONDS)
        }
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value))
    }

    fun get(key: String): Any? = redisTemplate.opsForValue().get(key)

    fun addList(
        key: String,
        value: Any,
    ) {
        redisTemplate.opsForList().rightPush(key, value)
    }

    fun <T> getList(
        key: String,
        type: Class<T>,
    ): List<T>? {
        val listOperations = redisTemplate.opsForList()
        val size = listOperations.size(key).takeIf { it != null && it > 0L } ?: return null
        val data = listOperations.leftPop(key, size) as List<*>
        return data.map { objectMapper.convertValue(it, type) }
    }
}

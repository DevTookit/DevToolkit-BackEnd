package com.project.api.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.project.api.config.properties.SecurityProperties
import com.project.api.web.dto.response.TokenResponse
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class AuthService(
    private val properties: SecurityProperties,
    private val redisService: RedisService,
) {
    fun create(email: String): TokenResponse {
        val (refreshToken, refreshTokenExpire) = createToken(email, properties.tokenRefresh.toLong())
        redisService.add(createKey(email), refreshToken, properties.tokenRefresh.toLong())
        val (accessToken, accessTokenExpire) = createToken(email, properties.tokenAccess.toLong())
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpire = accessTokenExpire.time,
            refreshTokenExpire = refreshTokenExpire.time,
        )
    }

    fun validate(
        email: String,
        refreshToken: String,
    ): Boolean {
        val token = redisService.get(createKey(email))?.toString() ?: return false
        return true
    }

    private fun createKey(email: String) = "REFRESH_$email"

    private fun createToken(
        email: String,
        period: Long,
    ): Pair<String, Date> {
        val now = Instant.now()
        val issuedAt = Date.from(now)
        val expiredAt = Date.from(now.plusMillis(period))
        val claimSet =
            JWTClaimsSet
                .Builder()
                .subject(email)
                .expirationTime(expiredAt)
                .issueTime(issuedAt)
                .build()

        val signedJWT =
            SignedJWT(
                JWSHeader
                    .Builder(JWSAlgorithm.HS256)
                    .build(),
                claimSet,
            )
        signedJWT.sign(MACSigner(properties.key))

        return signedJWT.serialize() to expiredAt
    }
}

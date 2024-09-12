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
) {
    fun create(email: String): TokenResponse =
        TokenResponse(
            accessToken = createToken(email, properties.tokenAccess.toLong()),
            refreshToken = createToken(email, properties.tokenRefresh.toLong()),
        )

    private fun createToken(
        email: String,
        period: Long,
    ): String {
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

        return signedJWT.serialize()
    }
}

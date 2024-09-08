package com.project.api.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.project.api.config.properties.SecurityProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class AuthService(
    private val properties: SecurityProperties,
) {
    fun createAccessToken(email: String): String {
        val now = Instant.now()
        val issuedAt = Date.from(now)
        val expiredAt = Date.from(now.plusMillis(properties.tokenAccess.toLong()))
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

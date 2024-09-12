package com.project.api.service

import jakarta.mail.Message
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
) {
    fun sendTmpPassword(
        email: String,
        tmpPassword: String,
    ) {
        val mailMessage =
            javaMailSender.createMimeMessage().apply {
                setSubject("[DevToolKit] 임시비밀번호 발급")
                setText(
                    "안녕하세요, 임시 비밀번호 발급해드렸습니다. <br/> 로그인 후 비밀번호를 변경해주세요. <br/> 임시 비밀번호 : $tmpPassword",
                    "UTF-8",
                    "html",
                )
                setFrom("DevToolKit")
                addRecipients(Message.RecipientType.TO, email)
            }

        javaMailSender.send(mailMessage)
    }
}

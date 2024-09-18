package com.project.api.service

import com.project.api.internal.EmailForm
import jakarta.mail.Message
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
) {
    fun send(
        email: String,
        code: String,
        type: EmailForm
    ) {
        val mailMessage =
            javaMailSender.createMimeMessage().apply {
                setSubject(type.subject)
                setText(
                    type.message+"<br/> 코드 : $code",
                    "UTF-8",
                    "html",
                )
                setFrom("DevToolKit")
                addRecipients(Message.RecipientType.TO, email)
            }

        javaMailSender.send(mailMessage)
    }
}

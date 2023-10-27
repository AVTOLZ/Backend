package dev.avt

import io.github.cdimascio.dotenv.dotenv
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

object Email {
    private val dotEnv = dotenv()

    private val properties = Properties().apply {
        this["mail.smtp.auth"] = true
        this["mail.smtp.starttls.enable"] = true
        this["mail.smtp.host"] = dotEnv["SMTP_SERVER"]
        this["mail.smtp.port"] = dotEnv["SMTP_PORT"]
        this["mail.smtp.ssl.trust"] = dotEnv["SMTP_SERVER"]
    }

    private val session = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(
                dotEnv["SMTP_USERNAME"],
                dotEnv["SMTP_PASSWORD"]
            )
        }
    })

    fun sendMail(recipient: String, subject: String, body: String) {
        val message = MimeMessage(session)

        message.setFrom(InternetAddress(dotEnv["SMTP_USERNAME"]))
        message.setRecipients(MimeMessage.RecipientType.TO, recipient)

        message.subject = subject
        message.setText(body)

        Transport.send(message)
    }

    init {

    }
}
package com.pin.pinapi.core.email.service

import com.pin.pinapi.core.email.exception.*
import com.pin.pinapi.core.email.repository.EmailRepository
import com.pin.pinapi.core.user.repository.UserRepository
import com.pin.pinapi.util.LogUtil.logger
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Service
class EmailService(
    val javaMailSender: JavaMailSender,
    val emailRepository: EmailRepository,
    val userRepository: UserRepository
) {


    private fun createMessage(to: String, key: String): MimeMessage {
        logger().info("보내는 대상 : ${to}")
        val message: MimeMessage = javaMailSender.createMimeMessage()
        message.addRecipients(MimeMessage.RecipientType.TO, to) //보내는 대상
        message.setSubject("Pin 이메일 인증 코드: $key") //제목

        var msg = ""
        msg += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">이메일 인증</h1>"
        msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 Pin 앱의 입력 화면에 입력하세요.</p>"
        msg += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">"
        msg += key
        msg += "</td></tr></tbody></table></div>"
        message.setText(msg, "utf-8", "html") //내용
        message.setFrom(InternetAddress("kyjdy@naver.com", "pinVerification")) //보내는 사람
        return message
    }

    @Transactional(readOnly = true)
    fun isEmailVerifiedOrRecentlySent(email: String) {
        val user = userRepository.findUserByEmailAddress(email)
        if (user != null) {
            throw com.pin.pinapi.core.email.exception.AlreadyVerifiedEmailException()
        }
    }

    @Synchronized
    fun sendMessage(to: String): com.pin.pinapi.core.email.dto.EmailDto.EmailResponse {
        isEmailVerifiedOrRecentlySent(to)

        val user: com.pin.pinapi.core.email.entity.EmailVerification? =
            emailRepository.findEmailVerificationByEmailAddress(to)
        val key = createKey()
        val expiredDate = Date.from(ZonedDateTime.now().plusMinutes(3).toInstant())
        if (user != null) {
            // 메일이 인증내역에 존재한다면
            user.expiredDate = expiredDate
            user.verificationKey = key
            emailRepository.save(user)
        } else {
            emailRepository.save(com.pin.pinapi.core.email.entity.EmailVerification(to, key, expiredDate, false))
        }
        val message: MimeMessage = createMessage(to, key)
        try { //예외처리
            javaMailSender.send(message)
        } catch (ex: MailException) {
            throw com.pin.pinapi.core.email.exception.SendEmailFailedException()
        }
        return com.pin.pinapi.core.email.dto.EmailDto.EmailResponse(to, expiredDate)
    }

    @Transactional
    fun verifyKey(email: String, key: String): Boolean {
        val user: com.pin.pinapi.core.email.entity.EmailVerification =
            emailRepository.findEmailVerificationByEmailAddress(email)
                ?: throw com.pin.pinapi.core.email.exception.InvalidEmailException()
        // 인증 기한이 지났을 경우
        if (user.expiredDate.before(Date.from(ZonedDateTime.now().toInstant()))) {
            throw com.pin.pinapi.core.email.exception.KeyExpiredException()
        }
        if (user.verificationKey == key) {
            user.verified = true
            emailRepository.save(user)
            return true
        }

        throw InvalidCodeException()
    }

    @Transactional
    fun isVerified(emailAddress: String) {
        val emailVerification: com.pin.pinapi.core.email.entity.EmailVerification? =
            emailRepository.findEmailVerificationByEmailAddress(emailAddress)
        if (emailVerification == null || !emailVerification.verified) {
            throw com.pin.pinapi.core.email.exception.NotVerifiedEmailException()
        }
        emailRepository.deleteByEmailAddress(emailVerification.emailAddress)
    }

    companion object {
        fun createKey(): String {
            val key = StringBuffer()
            val rnd = Random()
            for (i in 0..5) { // 인증코드 6자리
                key.append(rnd.nextInt(10))
            }
            return key.toString()
        }
    }
}
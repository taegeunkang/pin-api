package com.pin.pinapi.core.email

import com.pin.pinapi.core.email.service.EmailService
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EmailController(
    private val emailService: EmailService
) {
    // ? 물음표 제거 후 valid notblank 지운 후에 테스트 해보기

    @ApiOperation(value = "이메일 전송")
    @GetMapping("/email/send")
    fun requestEmail(@RequestParam email: String): com.pin.pinapi.core.email.dto.EmailDto.EmailResponse {
        return emailService.sendMessage(email)
    }

    @ApiOperation(value = "인증번호 확인")
    @GetMapping("/email/verify")
    fun verifyEmail(
        @RequestParam email: String,
        @RequestParam key: String
    ): com.pin.pinapi.core.email.dto.EmailDto.VerificationResponse {
        return com.pin.pinapi.core.email.dto.EmailDto.VerificationResponse(emailService.verifyKey(email, key))
    }
}
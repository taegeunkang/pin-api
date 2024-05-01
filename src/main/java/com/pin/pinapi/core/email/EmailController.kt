package com.pin.pinapi.core.email

import com.pin.pinapi.core.email.dto.EmailDto
import com.pin.pinapi.core.email.service.EmailService
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/email")
@RestController
class EmailController(
    private val emailService: EmailService
) {
    // ? 물음표 제거 후 valid notblank 지운 후에 테스트 해보기

    @ApiOperation(value = "이메일 전송")
    @GetMapping("/send")
    fun requestEmail(@RequestParam email: String): EmailDto.EmailResponse {
        return emailService.sendMessage(email)
    }

    @ApiOperation(value = "인증번호 확인")
    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam email: String,
        @RequestParam key: String
    ): EmailDto.VerificationResponse {
        return EmailDto.VerificationResponse(emailService.verifyKey(email, key))
    }
}
package com.pin.pinapi.core.email.dto

import java.util.*

class EmailDto {
    data class EmailResponse(val email: String, val expiredDate: Date)
    data class VerificationResponse(val verified: Boolean)
}
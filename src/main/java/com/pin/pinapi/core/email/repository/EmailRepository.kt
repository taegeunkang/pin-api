package com.pin.pinapi.core.email.repository

import com.pin.pinapi.core.email.entity.EmailVerification
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository : JpaRepository<EmailVerification, Long> {
    fun findEmailVerificationByEmailAddress(emailAddress: String): EmailVerification?
    
    fun deleteByEmailAddress(emailAddress: String)
}
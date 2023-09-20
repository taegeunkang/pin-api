package com.pin.pinapi.core.email.repository

import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository : JpaRepository<com.pin.pinapi.core.email.entity.EmailVerification, Long> {
    fun findEmailVerificationByEmailAddress(emailAddress: String): com.pin.pinapi.core.email.entity.EmailVerification?
    fun deleteByEmailAddress(emailAddress: String)
}
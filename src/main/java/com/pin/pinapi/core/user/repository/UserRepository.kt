package com.pin.pinapi.core.user.repository

import com.pin.pinapi.core.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findUserByEmailAddress(emailAddress: String): User?
}
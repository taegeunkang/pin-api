package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Noti
import com.pin.pinapi.core.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface NotiRepository : JpaRepository<Noti, Long> {
    fun findAllByUserOrderByCreatedDateDesc(user: User): List<Noti>

    fun findByUserAndPostId(user: User, postId: Long): Noti
}
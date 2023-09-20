package com.pin.pinapi.core.user.repository

import com.pin.pinapi.core.user.entity.Follow
import com.pin.pinapi.core.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FollowRepository : JpaRepository<Follow, Long> {

    // 팔로워 조회
    fun findByFromUser(user: User, page: Pageable): Page<Follow>

    // 팔로잉 조회
    fun findByToUser(user: User, page: Pageable): Page<Follow>


    fun findByFromUserAndToUser(fromUser: User, toUser: User): Follow?


    fun countFollowByToUserAndBannedIsFalse(user: User): Long

    fun countFollowByFromUserAndBannedIsFalse(user: User): Long

}
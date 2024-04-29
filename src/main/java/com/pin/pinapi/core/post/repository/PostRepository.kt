package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Post
import com.pin.pinapi.core.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PostRepository : JpaRepository<Post, Long> {
    fun findAllByUserOrderByCreatedDateDesc(user: User, pageable: Pageable): Page<Post>

    fun findAllByUser(user: User): List<Post>
    
    fun deleteByIdAndUser(id: Long, user: User)

    fun countByUser(user: User): Long

    // 홈에서 핵심 로직 테스트 required
    @Query("select p from Post p join Follow f on f.fromUser = :user and p.user = f.toUser where f.createdDate >= :date order by p.createdDate desc")
    fun findAllByUserAndFollowBeforeYesterday(@Param("user") user: User, @Param("date") date: LocalDateTime): List<Post>

}
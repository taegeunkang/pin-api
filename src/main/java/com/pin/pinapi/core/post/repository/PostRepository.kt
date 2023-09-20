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
    fun findAllByUserIdOrderByCreatedDateDesc(id: Long, pageable: Pageable): Page<Post>
    fun findAllByUserId(id: Long): List<Post>


    fun findPostById(id: Long): Post?

    fun deleteByIdAndUserId(id: Long, userId: Long)

    fun countByUser(user: User): Long

    @Query("select p from Post p join Follow f on f.fromUser = :user and p.user = f.toUser where f.createdDate >= :date order by p.createdDate desc")
    fun findAllByUserAndFollowBeforeYesterDay(@Param("user") user: User, @Param("date") date: LocalDateTime): List<Post>

}
package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Post
import com.pin.pinapi.core.post.entity.ThumbsUp
import com.pin.pinapi.core.post.entity.ThumbsUpId
import com.pin.pinapi.core.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ThumbsUpRepository : JpaRepository<ThumbsUp, ThumbsUpId> {

    fun countThumbsUpsByPost(post: Post): Long

    fun deleteThumbsUpByUserAndPost(user: User, post: Post)

    @Query("select t from ThumbsUp t where t.user = :user and t.post = :post")
    fun findThumbsUpByUserAndPost(@Param("user") user: User, @Param("post") post: Post): ThumbsUp?
}
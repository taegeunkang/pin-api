package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Mention
import com.pin.pinapi.core.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface MentionRepository : JpaRepository<Mention, Long> {

    fun findAllByPost(post: Post): List<Mention>
    fun findByPost(post: Post): Mention
}
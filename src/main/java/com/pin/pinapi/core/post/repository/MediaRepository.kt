package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Media
import com.pin.pinapi.core.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface MediaRepository : JpaRepository<Media, String> {

    fun findFirstByPost(post: Post): Media?

    fun findAllByPost(post: Post): List<Media>?

    fun findByName(name: String): Media?


}
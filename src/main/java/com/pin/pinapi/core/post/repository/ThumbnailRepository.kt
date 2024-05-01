package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Media
import com.pin.pinapi.core.post.entity.Thumbnail
import org.springframework.data.jpa.repository.JpaRepository

interface ThumbnailRepository : JpaRepository<Thumbnail, String> {

    fun findByMedia(media: Media): Thumbnail?
}
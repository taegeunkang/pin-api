package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import javax.persistence.*


@Entity
class Thumbnail(

    @Id
    val name: String,
    @Column(nullable = false)
    val size: Long,
    @Column(nullable = false, length = 10)
    val ext: String,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_name")
    val media: Media,

    ) : BaseTimeEntity()

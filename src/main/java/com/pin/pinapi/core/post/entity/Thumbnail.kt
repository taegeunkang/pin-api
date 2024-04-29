package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import javax.persistence.*


@Entity
class Thumbnail(

    @Column(nullable = false)
    val name: String,
    
    @OneToOne(fetch = FetchType.LAZY)
    val media: Media,
    @Column(nullable = false)
    val size: Long,

    ) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
}

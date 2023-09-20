package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import javax.persistence.*


@Entity
class Thumbnail(

    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val size: Long,
    @OneToOne
    @JoinColumn(name = "media_id")
    val media: Media

) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
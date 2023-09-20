package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import javax.persistence.*

@Entity
class ThumbsUp(
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User

) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}
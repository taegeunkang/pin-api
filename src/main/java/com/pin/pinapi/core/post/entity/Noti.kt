package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import javax.persistence.*

@Entity
class Noti(
    @Column(nullable = false)
    val message: String,
    @Column
    var pressed: Boolean,
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

}
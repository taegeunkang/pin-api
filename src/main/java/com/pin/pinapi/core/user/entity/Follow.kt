package com.pin.pinapi.core.user.entity

import javax.persistence.*

@Entity
class Follow(
    @ManyToOne(fetch = FetchType.LAZY)
    val fromUser: User,
    @ManyToOne(fetch = FetchType.LAZY)
    val toUser: User,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    var banned: Boolean = false
}
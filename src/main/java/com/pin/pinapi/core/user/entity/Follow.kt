package com.pin.pinapi.core.user.entity

import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(FollowId::class)
class Follow(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_email")
    val fromUser: User,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_email")
    val toUser: User,
) : BaseTimeEntity() {

    @Column(nullable = false)
    var banned: Boolean = false
}

data class FollowId(
    val fromUser: String? = null,
    val toUser: String? = null
) : Serializable
package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(ThumbsUpId::class)
class ThumbsUp(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    val post: Post,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email")
    val user: User

) : BaseTimeEntity()

data class ThumbsUpId(
    val post: Long? = null,
    val user: String? = null
) : Serializable

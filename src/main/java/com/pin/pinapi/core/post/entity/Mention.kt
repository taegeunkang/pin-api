package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(MentionId::class)
class Mention(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User

) : BaseTimeEntity()

data class MentionId(
    val post: Long? = null,
    val user: String? = null
) : Serializable
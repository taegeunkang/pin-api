package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import javax.persistence.*

@Entity
class Comment(
    @Column(nullable = false, length = 120)
    var content: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,
    @Column(nullable = true)
    val reply: Long?,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var writer: User?

) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0


}
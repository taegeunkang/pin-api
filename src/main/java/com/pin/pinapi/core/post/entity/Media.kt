package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import javax.persistence.*

@Entity
class Media(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val size: Long,
    @Column(nullable = false, length = 10)
    val ext: String,

    @ManyToOne
    @JoinColumn(name = "post_id")
    val post: Post

) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToOne(mappedBy = "media", cascade = [CascadeType.ALL], orphanRemoval = true)
    val thumbnail: Thumbnail? = null
}
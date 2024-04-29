package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import com.pin.pinapi.core.user.entity.User
import javax.persistence.*

@Entity
class Post(
    @Column(nullable = false)
    var content: String,
    @Column(nullable = false)
    val lat: Double,
    @Column(nullable = false)
    val lon: Double,
    @Column(nullable = false)
    val locationName: String,
    @Column(nullable = false)
    val isPrivate: Boolean = false,

    @ManyToOne
    val user: User,

    ) : BaseTimeEntity() {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val commentList: List<Comment> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mediaList: List<Media> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mentionList: List<Mention> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val thumbsUpList: List<ThumbsUp> = mutableListOf()
}
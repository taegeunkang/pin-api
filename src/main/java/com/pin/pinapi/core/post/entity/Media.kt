package com.pin.pinapi.core.post.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(MediaId::class)
class Media(
    @Id
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val size: Long,
    @Column(nullable = false, length = 10)
    val ext: String,

    @Id
    @ManyToOne
    val post: Post,

    ) : BaseTimeEntity() {
        
    @OneToOne(mappedBy = "media", cascade = [CascadeType.ALL], orphanRemoval = true)
    val thumbnail: Thumbnail? = null
}

data class MediaId(
    val name: String? = null,
    val post: Long? = null
) : Serializable
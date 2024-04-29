package com.pin.pinapi.core.user.entity

import javax.persistence.*

@Entity
class UserInfo(
    @Id
    val userId: String,
    @Column(nullable = false, unique = true)
    var nickName: String,
    @Column(nullable = false)
    var profileImg: String,
    @Column(nullable = false)
    var backgroundImg: String,
    @Column(nullable = true)
    var notificationToken: String?,

    ) {

    @MapsId("userId")
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User? = null
}
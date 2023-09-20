package com.pin.pinapi.core.user.entity

import javax.persistence.*

@Entity
class UserInfo(
    @Column(nullable = false, unique = true)
    var nickName: String,
    @Column(nullable = false)
    var profileImg: String,
    @Column(nullable = false)
    var backgroundImg: String,
    @Column(nullable = true)
    var notificationToken: String?,
    @OneToOne(cascade = [CascadeType.ALL])
    val user: User,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

}
package com.pin.pinapi.core.user.entity

import com.pin.pinapi.core.user.constants.Social
import javax.persistence.*

@Entity
class User(
    @Column(nullable = false, unique = true)
    val emailAddress: String,
    @Column(nullable = true)
    var password: String?,
    @Column(nullable = false)
    val loginType: Social = Social.NONE,


    ) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToOne(mappedBy = "user")
    val userInfo: UserInfo? = null


}
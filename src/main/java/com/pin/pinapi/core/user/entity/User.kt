package com.pin.pinapi.core.user.entity

import com.pin.pinapi.core.user.constants.Social
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class User(
    @Id
    val email: String,
    @Column(nullable = true)
    var password: String? = null,
    @Column(nullable = false)
    val loginType: Social = Social.NONE,
) : BaseTimeEntity() {

    @OneToOne(mappedBy = "user")
    val userInfo: UserInfo? = null


}
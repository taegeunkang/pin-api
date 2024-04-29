package com.pin.pinapi.core.email.entity

import com.pin.pinapi.core.user.entity.BaseTimeEntity
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class EmailVerification(
    @Id
    val emailAddress: String,
    @Column(nullable = false)
    var verificationKey: String,
    @Column(nullable = false)
    var expiredDate: Date,
    @Column(nullable = false)
    var verified: Boolean = false
) : BaseTimeEntity()
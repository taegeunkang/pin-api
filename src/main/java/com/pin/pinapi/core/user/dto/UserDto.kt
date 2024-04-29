package com.pin.pinapi.core.user.dto

import com.pin.pinapi.core.user.entity.User
import java.util.*

class UserDto {
    data class Error(val timestamp: Date, val status: Int, val code: String, val message: String)
    data class Login(val emailAddress: String, val password: String)
    data class LoginResponse(
        val emailAddress: String,
        val token: String,
        val tokenExpire: Date,
        val refreshToken: String,
        val refreshTokenExpire: Date,
        val isFirstLogin: Boolean
    )

    data class PasswordReset(val emailAddress: String, val password: String)
    data class Refresh(val emailAddress: String, val refreshToken: String)
    data class RefreshResponse(
        val username: String,
        val token: String,
        val tokenExpire: Date,
        val refreshToken: String,
        val refreshTokenExpire: Date
    )

    data class Register(val emailAddress: String, var password: String) {
        fun toEntity(): User {
            return User(emailAddress, password)
        }
    }

    data class OAuth(val provider: String, val accessToken: String, val nonce: String?)
    data class OAuthResponse(
        val emailAddress: String,
        val token: String,
        val tokenExpire: Date,
        val refreshToken: String,
        val refreshTokenExpire: Date,
        val firstLogin: Boolean
    )

    data class checkResponse(
        val id: String,
        val emailAddress: String
    )

    data class FollowerListDto(val userId: String, val word: String?, val page: Int, val size: Int)
    data class FollowerListResponseDto(val userId: String, val nickname: String, val profileImg: String)
    data class FollowingListDto(val userId: String, val word: String?, val page: Int, val size: Int)
    data class FollowingListResponseDto(val userId: String, val nickname: String, val profileImg: String)

    data class SearchDto(val word: String, val page: Int, val size: Int)

    data class SearchResponseDto(val userId: Long, val nickname: String, val profileImg: String)

    data class ProfileInitDto(val nickname: String)


    data class profileResponseDto(
        val userId: String,
        val nickname: String,
        val profileImg: String,
        val backgroundImg: String,
        val post: Long,
        val follower: Long,
        val following: Long,
        val followStatus: Long
    )


}

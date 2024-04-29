package com.pin.pinapi.core.user.repository

import com.pin.pinapi.core.user.entity.User
import com.pin.pinapi.core.user.entity.UserInfo
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserInfoRepository : JpaRepository<UserInfo, String> {
    fun findByNickName(nickname: String): UserInfo?
    fun findByUser(user: User): UserInfo?
    fun deleteByUser(user: User)

    fun findAllByNickNameContainingOrderByNickNameAsc(word: String, page: Pageable): List<UserInfo>

    @Query("select u from UserInfo  u join Follow f on f.fromUser = u.user where f.toUser = :user order by u.nickName asc")
    fun findByUserJoinFollowToUserOrderByNickNameAsc(@Param("user") user: User, page: Pageable): List<UserInfo>

    @Query("select u from UserInfo  u join Follow f on f.fromUser = u.user where f.toUser = :user order by u.nickName asc")
    fun findAllByUserJoinFollowToUserOrderByNickNameAsc(@Param("user") user: User): List<UserInfo>

    @Query("select u from UserInfo  u join Follow f on f.fromUser = u.user where f.toUser = :user and u.nickName like CONCAT('%',:word, '%') order by u.nickName asc")
    fun findByUserJoinFollowToUserOrderByNickNameContainingAsc(
        @Param("user") user: User,
        @Param("word") word: String,
        page: Pageable
    ): List<UserInfo>


    @Query("select u from UserInfo  u join Follow f on f.toUser = u.user where f.fromUser = :user order by u.nickName asc")
    fun findByUserJoinFollowFromUserOrderByNickNameAsc(@Param("user") user: User, page: Pageable): List<UserInfo>

    @Query("select u from UserInfo  u join Follow f on f.toUser = u.user where f.fromUser = :user and u.nickName like CONCAT('%',:word, '%') order by u.nickName asc")
    fun findByUserJoinFollowFromUserOrderByNickNameContainingAsc(
        @Param("user") user: User,
        @Param("word") word: String,
        page: Pageable
    ): List<UserInfo>
}
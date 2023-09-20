package com.pin.pinapi.core.user.service

import com.pin.pinapi.core.email.service.EmailService
import com.pin.pinapi.core.post.exception.ContentNotFoundException
import com.pin.pinapi.core.post.repository.PostRepository
import com.pin.pinapi.core.user.constants.Social
import com.pin.pinapi.core.user.dto.UserDto
import com.pin.pinapi.core.user.entity.Follow
import com.pin.pinapi.core.user.entity.User
import com.pin.pinapi.core.user.entity.UserInfo
import com.pin.pinapi.core.user.exception.*
import com.pin.pinapi.core.user.repository.FollowRepository
import com.pin.pinapi.core.user.repository.UserInfoRepository
import com.pin.pinapi.core.user.repository.UserRepository
import com.pin.pinapi.util.FileUtil
import com.pin.pinapi.util.LogUtil.logger
import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Service
class UserService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val jwtUtil: com.pin.pinapi.core.security.util.JWTUtil,
    val emailService: EmailService,
    val userInfoRepository: UserInfoRepository,
    val followRepository: FollowRepository,
    val postRepository: PostRepository,
) {

    @Transactional(readOnly = true)
    fun login(loginDTO: UserDto.Login): UserDto.LoginResponse {
        val user = userRepository.findUserByEmailAddress(loginDTO.emailAddress) ?: throw UserNotFoundException()
        val isMatch = passwordEncoder.matches(loginDTO.password, user.password)

        val isFirstLogin = if (userInfoRepository.findByUser(user) == null) true else false

        return if (isMatch) {
            val tokens = jwtUtil.generateTokens(loginDTO.emailAddress)
            val token = tokens["token"]
            val refreshToken = tokens["refreshToken"]
            UserDto.LoginResponse(
                user.id,
                loginDTO.emailAddress,
                token!!,
                jwtUtil.getExp("Bearer " + tokens["token"]),
                refreshToken!!,
                jwtUtil.getExp("Bearer " + tokens["refreshToken"]),
                isFirstLogin
            )
        } else {
            throw PasswordNotCorrectException()
        }
    }


    private fun getDataOfGoogle(url: String, token: String): String {
        try {
            val uri = URL(url) // 호출할 외부 API 를 입력한다.
            val conn: HttpURLConnection = uri.openConnection() as HttpURLConnection // header에 데이터 통신 방법을 지정한다.

            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer ${token}")
            conn.doOutput = true

            val inptStream = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            var response: String = ""

            while (true) {
                response += inptStream.readLine() ?: break
            }
            inptStream.close()
            conn.disconnect()

            val jsonParse: JSONParser = JSONParser()
            val jsonObj: JSONObject = jsonParse.parse(response) as JSONObject
            val userEmail = jsonObj["email"]
            return userEmail.toString()

        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ConnectionErrorException()
        }
    }


    private fun getDataOfKaKao(token: String, nonce: String): String {
        val payload = jwtUtil.getNonce(token) ?: throw InvalidTokenException()
        val payloadNonce = payload["nonce"].toString()
        val email = payload["email"].toString()
        if (nonce != payloadNonce) throw InvalidTokenException()
        return email
    }


    private fun google(token: String): String {
        val url = "https://www.googleapis.com/oauth2/v2/userinfo"
        return getDataOfGoogle(url, token)
    }

    private fun kakako(token: String, nonce: String?): String {
        if (nonce == null) throw NonceEmptyException()
        return getDataOfKaKao(token, nonce)
    }


    // auth provider 추가 될 때 마다 수정
    private fun oauthCheck(oauth: UserDto.OAuth): String {
        return when (getProviderEnum(oauth.provider)) {
            Social.GOOGLE -> google(oauth.accessToken)
            Social.KAKAO -> kakako(oauth.accessToken, oauth.nonce)
            else -> ""
        }
    }

    private fun getProviderEnum(provider: String): Social {
        return when (provider) {
            "google" -> Social.GOOGLE
            "kakao" -> Social.KAKAO
            else -> Social.NONE
        }
    }

    private fun isRegisteredWithOAuth(email: String): Long {
        val user: User = userRepository.findUserByEmailAddress(email) ?: return -1L
        return user.id
    }

    private fun createRandomNickname(): String {
        return "user-" + UUID.randomUUID().toString().substring(0, 6)
    }


    private fun createOAuthUser(email: String, provider: Social): Long {
        val user: User = userRepository.save(User(email, null, provider))
        logger().info("사용자 생성 email : {}", user.emailAddress)
        return user.id
    }

    fun oauthLogin(oauth: UserDto.OAuth): UserDto.OAuthResponse {
        val email: String = oauthCheck(oauth)

        val tokens: Map<String, String> = jwtUtil.generateTokens(email)
        var id: Long = isRegisteredWithOAuth(email)
        logger().info("아이디 존재 여부 : {}", if (id == -1L) "false" else "true")
        var firstLogin = id == -1L
        if (id == -1L) id = createOAuthUser(email, getProviderEnum(oauth.provider))

        return UserDto.OAuthResponse(
            id,
            email,
            tokens["token"]!!,
            jwtUtil.getExp(tokens["token"]),
            tokens["refreshToken"]!!,
            jwtUtil.getExp(tokens["refreshToken"]),
            firstLogin
        )


    }

    @Transactional
    fun register(registerDTO: UserDto.Register): UserDto.RegisterResponse {
        isRegisterAvailable(registerDTO)
        registerDTO.password = passwordEncoder.encode(registerDTO.password)
        val user: User = userRepository.save(registerDTO.toEntity())
        return UserDto.RegisterResponse(user.id)
    }

    fun reIssue(refreshDto: UserDto.Refresh): UserDto.RefreshResponse {
        val refreshTokenUsername = jwtUtil.getSubject(refreshDto.refreshToken)

        return if (refreshDto.emailAddress == refreshTokenUsername) {
            val tokens = jwtUtil.generateTokens(refreshTokenUsername)
            val token = tokens["token"]
            val refreshToken = tokens["refreshToken"]
            UserDto.RefreshResponse(
                refreshTokenUsername,
                token!!,
                jwtUtil.getExp("Bearer ${token}"),
                refreshToken!!,
                jwtUtil.getExp("Bearer ${refreshToken}")
            )
        } else {
            logger().info("token : ${refreshTokenUsername} refreshToken : ${refreshTokenUsername}")
            throw TokenNotMatchException()
        }
    }

    @Transactional
    fun isRegisterAvailable(registerDto: UserDto.Register) {
        emailService.isVerified(registerDto.emailAddress)

        userRepository.findUserByEmailAddress(registerDto.emailAddress)
            .let { a: User? -> if (a != null) throw UserExistsException() }

    }

    @Transactional
    fun resetPassword(passwordResetDto: UserDto.PasswordReset) {
        val (emailAddress: String, password: String) = passwordResetDto

        emailService.isVerified(emailAddress)
        val user = userRepository.findUserByEmailAddress(emailAddress)
        logger().info("user {} pasword reset to {}", emailAddress, password)

        if (user == null) {
            throw UserNotFoundException()
        } else if (user.loginType != Social.NONE) {
            throw SocialRegisteredException()
        }

        user.password = passwordEncoder.encode(password)
    }

    @Transactional(readOnly = true)
    fun isDuplicated(nickname: String) {
        val userInfo: UserInfo? = userInfoRepository.findByNickName(nickname)
        userInfo.let { a: UserInfo? -> if (a != null) throw NicknameExistsException() }
    }

    @Transactional
    fun updateNickname(token: String, nickname: String) {
        val emailAddress = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        val user = userRepository.findUserByEmailAddress(emailAddress) ?: throw UserNotFoundException()

        isDuplicated(nickname)

        val userInfo: UserInfo? = userInfoRepository.findByUserId(user.id)
        // User가 존재한다면 userInfo 또한 무조건 존재한다.
        val old = userInfo!!.nickName
        userInfo!!.nickName = nickname
        logger().info("user nickname change from {} to {}", old, nickname)

    }

    @Transactional
    fun deleteUser(userEmail: String, token: String) {
        val emailAddress = jwtUtil.getSubject(token) ?: throw InvalidTokenException()

        if (emailAddress != userEmail) throw EmailAndTokenNotMatchException()

        val user = userRepository.findUserByEmailAddress(userEmail) ?: throw UserNotFoundException()
        userInfoRepository.deleteByUserId(user.id)
    }

    @Transactional(readOnly = true)
    fun checkLoggedIn(token: String): UserDto.checkResponse {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject)
        user ?: throw UserNotFoundException()

        return UserDto.checkResponse(user.id, user.emailAddress)
    }


    @Transactional(readOnly = true)
    fun findFollower(followerListDto: UserDto.FollowerListDto): List<UserDto.FollowerListResponseDto> {

        val user = userRepository.findUserById(followerListDto.userId) ?: throw UserNotFoundException()
        var f: List<UserInfo>
        if (followerListDto.word == null) {
            f =
                userInfoRepository.findByUserJoinFollowToUserOrderByNickNameAsc(
                    user,
                    PageRequest.of(followerListDto.page, followerListDto.size)
                )
        } else {
            f = userInfoRepository.findByUserJoinFollowToUserOrderByNickNameContainingAsc(
                user,
                followerListDto.word,
                PageRequest.of(followerListDto.page, followerListDto.size)
            )
        }
        val followers = mutableListOf<UserDto.FollowerListResponseDto>()
        f.forEach { ff ->
            followers.add(UserDto.FollowerListResponseDto(ff.user.id, ff.nickName, ff.profileImg))
        }

        return followers
    }

    // 팔로잉 목록
    @Transactional(readOnly = true)
    fun findFollowing(followingListDto: UserDto.FollowingListDto): List<UserDto.FollowingListResponseDto> {

        val user = userRepository.findUserById(followingListDto.userId) ?: throw UserNotFoundException()
        var f: List<UserInfo>
        if (followingListDto.word == null) {
            f =
                userInfoRepository.findByUserJoinFollowFromUserOrderByNickNameAsc(
                    user,
                    PageRequest.of(followingListDto.page, followingListDto.size)
                )
        } else {
            f = userInfoRepository.findByUserJoinFollowFromUserOrderByNickNameContainingAsc(
                user,
                followingListDto.word,
                PageRequest.of(followingListDto.page, followingListDto.size)
            )
        }
        val followers = mutableListOf<UserDto.FollowingListResponseDto>()
        f.forEach { ff ->
            followers.add(UserDto.FollowingListResponseDto(ff.user.id, ff.nickName, ff.profileImg))
        }

        return followers
    }

    @Transactional
    fun addOrRemoveFollow(userId: Long, token: String): Long {
        val subject = jwtUtil.getSubject(token)
        val fromUser = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val toUser = userRepository.findUserById(userId) ?: throw UserNotFoundException()
        val f = followRepository.findByFromUserAndToUser(fromUser, toUser)
        var f1: Boolean = true
        if (f != null) {
            followRepository.delete(f)
            f1 = false
        } else {
            followRepository.save(Follow(fromUser, toUser))
            f1 = true
        }


        val f2 = followRepository.findByFromUserAndToUser(toUser, fromUser) != null
        val followStatus: Long = if (!f1 && !f2) {
            1 // 양쪽다 언팔
        } else if (!f1) {
            2 // 상대방만 나를 팔로잉
        } else {
            3 // 내가 팔로잉 or 맞팔
        }
        return followStatus

    }

    @Transactional
    fun blockFollower(userId: Long, token: String) {
        val subject = jwtUtil.getSubject(token)
        val toUser = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val fromUser = userRepository.findUserById(userId) ?: throw UserNotFoundException()
        val f = followRepository.findByFromUserAndToUser(fromUser, toUser) ?: throw ContentNotFoundException()
        f.banned = true
    }

    @Transactional
    fun search(searchDto: UserDto.SearchDto, token: String): List<UserDto.SearchResponseDto> {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
//        val blockedList = followRepository.findByFromUser(user, PageRequest.of(searchDto.page, searchDto.size))
//            .filter { u -> !u.banned }.map { u1 -> u1.toUser }.toList()

        val searchResult = userInfoRepository.findAllByNickNameContainingOrderByNickNameAsc(
            searchDto.word,
            PageRequest.of(searchDto.page, searchDto.size)
        )
            .map { m -> UserDto.SearchResponseDto(m.id, m.nickName, m.profileImg) }.toList()

        return searchResult
    }


    fun initProfile(profileInitDto: UserDto.ProfileInitDto, token: String) {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()

        isDuplicated(profileInitDto.nickname)

        if (userInfoRepository.findByUser(user) != null) {
            throw AlreadyInitUserException()
        }

        userInfoRepository.save(
            UserInfo(
                profileInitDto.nickname,
                "default-profile.png",
                "default-background.png",
                null,
                user
            )
        )

    }

    @Transactional
    fun setNotificationKey(fcmToken: String, token: String) {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val userInfo = userInfoRepository.findByUser(user)
        userInfo!!.notificationToken = fcmToken
    }

    // 팔로워 조회
    fun getUserProfile(userId: Long, token: String): UserDto.profileResponseDto {
        val subject = jwtUtil.getSubject(token)
        val fromUser = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val toUser = userRepository.findUserById(userId) ?: throw UserNotFoundException()
        val userInfo = userInfoRepository.findByUser(toUser) ?: throw NotInitUserException()
        val posts: Long = postRepository.countByUser(toUser)
        val follower: Long = followRepository.countFollowByToUserAndBannedIsFalse(toUser)
        val following: Long = followRepository.countFollowByFromUserAndBannedIsFalse(toUser)
        val f1 = followRepository.findByFromUserAndToUser(fromUser, toUser) != null
        val f2 = followRepository.findByFromUserAndToUser(toUser, fromUser) != null
        val followStatus: Long = if (fromUser.id == toUser.id) {
            0 // 내 페이지
        } else if (!f1 && !f2) {
            1 // 양쪽다 언팔
        } else if (!f1) {
            2 // 상대방만 나를 팔로잉
        } else {
            3 // 내가 팔로잉 or 맞팔
        }

        return UserDto.profileResponseDto(
            toUser.id,
            userInfo.nickName,
            userInfo.profileImg,
            userInfo.backgroundImg,
            posts,
            follower,
            following,
            followStatus
        )
    }

    @Transactional
    fun updateProfileImage(profileImage: MultipartFile?, token: String) {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val userInfo = userInfoRepository.findByUser(user) ?: throw UserNotFoundException()
        var fileName: String = "default-profile.png"
        if (profileImage != null) {
            fileName = FileUtil.fileSave(profileImage, "png")
        }

        userInfo.profileImg = fileName

    }


    @Transactional
    fun updateBackgroundImage(backgroundImage: MultipartFile?, token: String) {
        val subject = jwtUtil.getSubject(token)
        val user = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val userInfo = userInfoRepository.findByUser(user) ?: throw UserNotFoundException()
        var fileName: String = "default-background.png"
        if (backgroundImage != null) {
            fileName = FileUtil.fileSave(backgroundImage, "png")
        }

        userInfo.backgroundImg = fileName

    }


}



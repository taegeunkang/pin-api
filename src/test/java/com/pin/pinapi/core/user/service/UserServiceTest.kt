package com.pin.pinapi.core.user.service

import com.trep.trepapi.core.email.repository.EmailRepository
import com.trep.trepapi.core.user.dto.UserDto
import com.trep.trepapi.core.user.repository.FollowRepository
import com.trep.trepapi.core.user.repository.UserInfoRepository
import com.trep.trepapi.core.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime
import java.util.*

@SpringBootTest
@ActiveProfiles("dev")
class UserServiceTest(
    @Autowired val userService: UserService,
    @Autowired val followRepository: FollowRepository,
    @Autowired val userRepository: UserRepository,
    @Autowired val userInfoRepository: UserInfoRepository,
    @Autowired val emailRepository: EmailRepository
) {

    @BeforeEach
    fun setUp() {

    }

    @Test
    fun 회원가입_성공() {
        //이메일 인증 성공 전제
        emailRepository.save(
            com.pin.pinapi.core.email.entity.EmailVerification(
                "kyjdy@naver.com",
                "TREP",
                Date.from(ZonedDateTime.now().toInstant()),
                true
            )
        )

        val registerDto = UserDto.Register("kyjdy@naver.com", "xorms123")
        val userId: Long = userService.register(registerDto).id
        val user = userRepository.findUserById(userId) ?: throw Exception()
        println(registerDto.emailAddress)
        println(user.emailAddress)
        assert(registerDto.emailAddress.equals(user.emailAddress))
    }

    @Test
    fun 로그인_성공() {
        val loginResponse = userService.login(UserDto.Login("kyjdy@naver.com", "xorms123"))

    }

    @Test
    fun 로그인_실패() {
    }

    @Test
    fun 토큰_재_발급_성공() {

    }

    @Test
    fun 닉네임_중복_O() {

    }

    @Test
    fun 닉네임_중복_X() {

    }

    @Test
    fun 닉네임_변경_성공() {

    }

    @Test
    fun 닉네임_변경_실패() {

    }

    @Test
    fun 회원_탈퇴_성공() {

    }

    @Test
    fun 팔로우_신청() {

    }

    @Test
    fun 팔로우_취소() {

    }

    @Test
    fun 팔로잉_목록_조회() {

    }

    @Test
    fun 팔로잉_목록_없음() {

    }

    @Test
    fun 팔로워_목록_조회() {

    }

    @Test
    fun 팔로워_목록_없음() {

    }

    @Test
    fun 팔로워_차단() {

    }

    @Test
    fun 사용자_검색() {

    }

    @Test
    fun 사용자_검색_결과없음() {

    }

    @Test
    fun 사용자_프로필_초기설정_성공() {

    }

    @Test
    fun 사용자_프로필_초기설정_실패() {

    }


}
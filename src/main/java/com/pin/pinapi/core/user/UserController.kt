package com.pin.pinapi.core.user

import com.pin.pinapi.core.user.dto.UserDto
import com.pin.pinapi.core.user.service.UserService
import com.pin.pinapi.util.FileUtil
import com.pin.pinapi.util.LogUtil.logger
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping(value = ["/user"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class UserController(val userService: UserService) {

    @ApiOperation(value = "회원가입")
    @PostMapping(value = ["/register"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun register(@RequestBody registerDTO: UserDto.Register) {
        userService.register(registerDTO)
    }

    @ApiOperation(value = "로그인")
    @PostMapping(value = ["/login"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun login(@RequestBody loginDTO: UserDto.Login): UserDto.LoginResponse {
        return userService.login(loginDTO)
    }

    @ApiOperation(value = "소셜 로그인", notes = "firstLogin == true라면 닉네임 설정 화면으로 이동")
    @PostMapping("/login/oauth")
    fun oauthLogin(@RequestBody oAuth: UserDto.OAuth): UserDto.OAuthResponse {
        logger().info("oauth login provider : {}", oAuth.provider)
        logger().info("access-token : {}", oAuth.accessToken)
        return userService.oauthLogin(oAuth)
    }

    @ApiOperation(value = "토큰 재 발급")
    @PostMapping(value = ["/refresh"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun refresh(@RequestBody refreshDto: UserDto.Refresh): UserDto.RefreshResponse {
        return userService.reIssue(refreshDto)
    }

    @ApiOperation(value = "닉네임 중복 확인")
    @GetMapping(value = ["/nickname/duplicate"])
    fun duplicate(@RequestParam nickname: String) {
        userService.isDuplicated(nickname)
    }

    @ApiOperation(value = "비밀번호 재설정")
    @PostMapping(value = ["/reset/password"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun resetPassword(@RequestBody passwordResetDto: UserDto.PasswordReset) {
        userService.resetPassword(passwordResetDto)
    }

    @ApiOperation(value = "닉네임 변경")
    @PatchMapping(value = ["/nickname/update"])
    fun updateNickname(
        @RequestHeader("Authorization") token: String,
        @RequestParam nickname: String
    ) {
        userService.updateNickname(token, nickname)
    }

    @ApiOperation(value = "회원 탈퇴")
    @DeleteMapping("/delete")
    fun deleteUser(@RequestParam emailAddress: String, @RequestHeader("Authorization") token: String) {
        userService.deleteUser(emailAddress, token)
    }

    @ApiOperation(value = "로그인 여부 확인")
    @PostMapping("/check")
    fun checkLoggedIn(@RequestHeader("Authorization") token: String): UserDto.checkResponse {
        return userService.checkLoggedIn(token)
    }


    @ApiOperation("팔로워 목록 조회")
    @GetMapping("/follower/list")
    fun findFollower(@ModelAttribute followerListDto: UserDto.FollowerListDto): List<UserDto.FollowerListResponseDto> {
        return userService.findFollower(followerListDto)
    }

    @ApiOperation("팔로잉 목록 죄회")
    @GetMapping("/following/list")
    fun findFollwing(@ModelAttribute followingListDto: UserDto.FollowingListDto): List<UserDto.FollowingListResponseDto> {
        return userService.findFollowing(followingListDto)
    }

    @ApiOperation("팔로우 추가/취소 (토글)")
    @PostMapping("/follow")
    fun follow(@RequestParam("userId") userId: String, @RequestHeader("Authorization") token: String): Long {
        return userService.addOrRemoveFollow(userId, token)
    }


    @ApiOperation("팔로워 차단/삭제")
    @PostMapping("/follower/block")
    fun blockFollower(@RequestParam("userId") userId: String, @RequestHeader("Authorization") token: String) {
        userService.blockFollower(userId, token)
    }

    @ApiOperation("사용자 검색")
    @PostMapping("/search")
    fun searchUser(
        @RequestBody searchDto: UserDto.SearchDto,
        @RequestHeader("Authorization") token: String
    ): List<UserDto.SearchResponseDto> {
        val search = userService.search(searchDto, token)
        return search
    }

    @ApiOperation("사용자 프로필 초기 설정")
    @PostMapping("/profile/init", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun initProfile(
        @RequestBody profileInitDto: UserDto.ProfileInitDto,
        @RequestHeader("Authorization") token: String
    ) {
        userService.initProfile(profileInitDto, token)
    }

    @ApiOperation("파이어베이스 토큰 업데이트")
    @PostMapping("/notification/init")
    fun setNotificationKey(@RequestParam fcmToken: String, @RequestHeader("Authorization") token: String) {
        userService.setNotificationKey(fcmToken, token)
    }

    @ApiOperation("사용자 프로필 조회")
    @PostMapping("/profile/info")
    fun getUserInfo(
        @RequestParam("userId") userId: String,
        @RequestHeader("Authorization") token: String
    ): UserDto.profileResponseDto {
        return userService.getUserProfile(userId, token)
    }

    @ApiOperation(value = "프로필 이미지 조회")
    @GetMapping("/profile/image", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getImage(@RequestParam watch: String): ByteArray {
        logger().info("프로필 이미지 조회 : {} ", watch)
        return FileUtil.getImage(watch)
    }

    @ApiOperation(value = "프로필 이미지 변경")
    @PostMapping("/profile/update/profileImage")
    fun updateProfileImage(
        @RequestPart("profileImage") profileImage: MultipartFile?,
        @RequestHeader("Authorization") token: String
    ) {
        userService.updateProfileImage(profileImage, token)
    }

    @ApiOperation(value = "프로필 배경 변경")
    @PostMapping("/profile/update/backgroundImage")
    fun updateBackgroundImage(
        @RequestPart("backgroundImage") backgroundImage: MultipartFile?,
        @RequestHeader("Authorization") token: String
    ) {
        userService.updateBackgroundImage(backgroundImage, token)
    }


}
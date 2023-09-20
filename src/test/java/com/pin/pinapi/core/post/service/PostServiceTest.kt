package com.pin.pinapi.core.post.service

import com.trep.trepapi.core.post.repository.*
import com.trep.trepapi.core.user.repository.UserInfoRepository
import com.trep.trepapi.core.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev")
class PostServiceTest(
    @Autowired val postService: PostService,
    @Autowired val thumbnailRepository: ThumbnailRepository,
    @Autowired val mediaRepository: MediaRepository,
    @Autowired val userRepository: UserRepository,
    @Autowired val userInfoRepository: UserInfoRepository,
    @Autowired val tagRepository: TagRepository,
    @Autowired val thumbsUpRepository: ThumbsUpRepository,
    @Autowired val commentRepository: CommentRepository,
) {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun 사용자_전체_게시글_조회_메인_홈() {

    }

    @Test
    fun 사용자_홈_전체_게시글_조회_없음() {

    }

    @Test
    fun MyList_조회_첫_페이지() {

    }

    @Test
    fun MyList_조회_마지막_페이지() {

    }

    @Test
    fun MyList_조회_내용_없음() {

    }

    @Test
    fun 게시글_생성_성공() {

    }


    @Test
    fun 게시글_삭제() {

    }

    @Test
    fun 게시글_수정() {

    }

    @Test
    fun 특정_게시글_세부_조회() {

    }

    @Test
    fun 엄지척() {

    }

    @Test
    fun 엄지척_취소() {

    }

    @Test
    fun 댓글_작성() {

    }

    @Test
    fun 대댓글_작성() {

    }

    @Test
    fun 댓글_조회() {

    }

    @Test
    fun 댓글_없음() {

    }
}
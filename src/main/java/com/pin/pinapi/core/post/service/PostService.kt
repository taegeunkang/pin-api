package com.pin.pinapi.core.post.service

import com.pin.pinapi.core.notification.service.FirebaseMessagingService
import com.pin.pinapi.core.post.dto.PostDto
import com.pin.pinapi.core.post.entity.*
import com.pin.pinapi.core.post.exception.ContentNotFoundException
import com.pin.pinapi.core.post.exception.MediaNotFoundException
import com.pin.pinapi.core.post.exception.NotPermittedException
import com.pin.pinapi.core.post.repository.*
import com.pin.pinapi.core.user.entity.User
import com.pin.pinapi.core.user.entity.UserInfo
import com.pin.pinapi.core.user.exception.InvalidTokenException
import com.pin.pinapi.core.user.exception.UserNotFoundException
import com.pin.pinapi.core.user.repository.UserInfoRepository
import com.pin.pinapi.core.user.repository.UserRepository
import com.pin.pinapi.util.FileUtil
import com.pin.pinapi.util.LogUtil.logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class PostService(
    val postRepository: PostRepository,
    val thumbnailRepository: ThumbnailRepository,
    val mediaRepository: MediaRepository,
    val userRepository: UserRepository,
    val userInfoRepository: UserInfoRepository,
    val thumbsUpRepository: ThumbsUpRepository,
    val commentRepository: CommentRepository,
    val mentionRepository: MentionRepository,
    val firebaseMessagingService: FirebaseMessagingService,
    val notiRepository: NotiRepository,
    val jwtUtil: com.pin.pinapi.core.security.util.JWTUtil
) {

    // 미디어 컨텐츠(사진, 동영상)는 파일 명만 전달하고
    // 따로 반환
    // id : 사용자 id
    @Transactional(readOnly = true)
    fun findAllMapPosts(id: Long, token: String): PostDto.PostMapAllResponse {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        // subject == email 이메일이 회원 인지 check
        val user: User = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val userInfo: UserInfo = userInfoRepository.findByUser(user) ?: throw UserNotFoundException() //수정 예정

        val result: MutableList<PostDto.ContentDtoResponse> = mutableListOf()
        val myContents: List<Post> = postRepository.findAllByUserId(id)
        val currentTime: LocalDateTime = LocalDateTime.now()

        val followContents: List<Post> =
            postRepository.findAllByUserAndFollowBeforeYesterDay(user, LocalDateTime.now().minusHours(24))
        val contents = myContents + followContents
        if (contents.isEmpty()) throw ContentNotFoundException()

        for (c in contents) {
            var fileName = ""
            val media = mediaRepository.findFirstByPost(c)
            fileName = if (media != null) media.name else userInfo.profileImg
            // 첫번째 미디어가 동영상이면 썸네일 반환
            if (media != null && media.ext == "mp4") {
                val thumbnail = thumbnailRepository.findByMedia(media) ?: throw ContentNotFoundException()
                fileName = thumbnail.name
            }
            val date: Date = Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(9)))
            val detail = findPost(c.id, token)
            result.add(PostDto.ContentDtoResponse(c.id, c.user.id, c.lat, c.lon, fileName, date, detail))
        }

        return PostDto.PostMapAllResponse(result)
    }

    /**
     * 같은 마이페이지인데 리스트 형태(컨텐츠의 사진 전체를 보여줌)
     */
    fun findMyListAll(postMyList: PostDto.PostMyList): List<PostDto.PostMyListResponse> {
        val myList: MutableList<PostDto.PostMyListResponse> = mutableListOf()
        val posts: Page<Post> =
            postRepository.findAllByUserIdOrderByCreatedDateDesc(
                postMyList.userId,
                PageRequest.of(postMyList.page, postMyList.size)
            )
        if (posts.isEmpty) throw ContentNotFoundException()

        for (p in posts) {
            val medias = mediaRepository.findAllByPost(p) ?: throw MediaNotFoundException()
            val imgs = mutableListOf<String>() // 사진
            for (media in medias) {
                var img = media.name
                if (media.ext == "mp4") {
                    val thumbnail = thumbnailRepository.findByMedia(media) ?: throw MediaNotFoundException()
                    img = thumbnail.name
                }

                imgs.add(img)
            }

            val date: Date = Date.from(p.createdDate!!.toInstant(ZoneOffset.ofHours(9)))
            myList.add(PostDto.PostMyListResponse(p.id, imgs, p.locationName, date))
        }

        return myList
    }

    // id : 컨텐츠 id
    // 태그가 없는 게시글의 경우 프론트 백 둘다 핸들링 해야함
    @Transactional(readOnly = true)
    fun findPost(id: Long, token: String): PostDto.PostResponse {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        val user: User = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()

        val content: Post = postRepository.findPostById(id) ?: throw ContentNotFoundException()
        val userInfo = userInfoRepository.findByUserId(content.user.id) ?: throw UserNotFoundException()

        val medias: List<Media>? = mediaRepository.findAllByPost(content)
        var media1 = mutableListOf<String>()
        if (medias != null) {
            for (media in medias) {
                media1.add(media.name)
            }
        }


        val date: Date = Date.from(content.createdDate!!.toInstant(ZoneOffset.ofHours(9)))

        // 좋아요 수
        val thumbsUpCount = thumbsUpRepository.countThumbsUpsByPost(content)

        // 요청자가 좋아요 했는지
        val isLikedBefore: Boolean =
            thumbsUpRepository.findThumbsUpByUserAndPost(user, content) != null
        // 댓글 수
        val commentCount: Long = commentRepository.countCommentByPostAndReplyIsNull(content)

        val mentionResult = mutableListOf<PostDto.ProfileSummaryResponseDto>()
        val mentionList: List<Long> = mentionRepository.findAllByPost(content).map { it.user.id }

        for (mm in mentionList) {
            val mentionUserInfo: UserInfo = userInfoRepository.findByUserId(mm) ?: throw UserNotFoundException()
            mentionResult.add(
                PostDto.ProfileSummaryResponseDto(
                    mentionUserInfo.id,
                    mentionUserInfo.nickName,
                    mentionUserInfo.profileImg
                )
            )
        }


        return PostDto.PostResponse(
            content.id,
            userInfo.nickName,
            content.user.id,
            userInfo.profileImg,
            content.content,
            media1,
            content.locationName,
            isLikedBefore,
            thumbsUpCount,
            commentCount,
            mentionResult,
            date
        )

    }

    @Transactional(readOnly = true)
    fun findAllPost(userId: Long, token: String, page: Int, size: Int): List<PostDto.PostResponse> {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        logger().info("${page}, ${size}")
        // subject == email 이메일이 회원 인지 check
        val user: User = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()

        val contents: Page<Post> =
            postRepository.findAllByUserIdOrderByCreatedDateDesc(userId, PageRequest.of(page, size))
        val userInfo = userInfoRepository.findByUserId(userId) ?: throw UserNotFoundException()

        var result = mutableListOf<PostDto.PostResponse>()
        for (c in contents) {

            val medias: List<Media>? = mediaRepository.findAllByPost(c)
            var media1 = mutableListOf<String>()

            if (medias != null) {
                for (media in medias) {
                    media1.add(media.name)
                }
            }


            val date: Date = Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(0)))

            // 좋아요 수
            val thumbsUpCount = thumbsUpRepository.countThumbsUpsByPost(c)

            // 요청자가 좋아요 했는지
            val isLikedBefore =
                thumbsUpRepository.findThumbsUpByUserAndPost(user, c) != null

            // 댓글 수
            val commentCount: Long = commentRepository.countCommentByPostAndReplyIsNull(c)
            // 멘션
            val mentionResult = mutableListOf<PostDto.ProfileSummaryResponseDto>()
            val mentionList: List<Long> = mentionRepository.findAllByPost(c).map { it.user.id }

            for (mm in mentionList) {
                val mentionUserInfo: UserInfo = userInfoRepository.findByUserId(mm) ?: throw UserNotFoundException()
                mentionResult.add(
                    PostDto.ProfileSummaryResponseDto(
                        mentionUserInfo.id,
                        mentionUserInfo.nickName,
                        mentionUserInfo.profileImg
                    )
                )
            }

            result.add(
                PostDto.PostResponse(
                    c.id,
                    userInfo.nickName,
                    user.id,
                    userInfo.profileImg,
                    c.content,
                    media1,
                    c.locationName,
                    isLikedBefore,
                    thumbsUpCount,
                    commentCount,
                    mentionResult,
                    date
                )
            )

        }

        return result

    }

    // 사진, 동영상 테이블 따로 두지만
    // 로컬 경로는 한 폴더에 넣고 파일 명으로 접근
    // 썸네일 테이블 따로 구성
    @Transactional
    fun create(post: PostDto.PostCreateDto, token: String) {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        // subject == email 이메일이 회원 인지 check
        val user: User = userRepository.findUserByEmailAddress(subject) ?: throw UserNotFoundException()
        val userInfo: UserInfo = userInfoRepository.findByUser(user)!!
        val content: Post = post.toPost(user)
        val contentSaved: Post = postRepository.save(content)
        var videoCnt = 0
        if (post.mediaFiles != null) {
            for (media in post.mediaFiles) {
                // 컨텐츠 타입
                val ext: String = if (media.contentType?.startsWith("video") == true) "mp4" else "png"
                val name: String = FileUtil.fileSave(media, ext)
                val size: Long = media.size
                val m: Media = post.toMedia(name, size, ext, contentSaved)
                val savedMedia = mediaRepository.save(m)
                // 비디오라면 썸네일도 저장
                if (ext.equals("mp4")) {
                    val thumbnail = post.thumbnailFiles!!.get(videoCnt)
                    val tName = FileUtil.fileSave(thumbnail, "png")
                    val tSize = thumbnail.size
                    val t = post.toThumbnail(savedMedia, tName, tSize)
                    thumbnailRepository.save(t)
                    videoCnt++
                }

            }
        }
        // 멘션
        if (post.mention != null) {
            for (m in post.mention) {
                val u = userRepository.findUserById(m) ?: throw UserNotFoundException()
                mentionRepository.save(Mention(contentSaved, u))
            }
        }

        // 알림 보내기
        val followers: List<UserInfo> = userInfoRepository.findAllByUserJoinFollowToUserOrderByNickNameAsc(user)
        for (follower in followers) {
            if (follower.notificationToken != null) {
                val title = "게시글 알림"
                val message = "팔로우 중인 ${userInfo.nickName}님이 새로운 게시글을 업로드했습니다."
                sendNotification(title, message, follower.notificationToken!!, contentSaved, follower.user)
            }

        }

    }


    // 컨텐츠 내용 수정
    // id : 컨텐츠 id
    @Transactional
    fun editPostContent(editPostDto: PostDto.EditPostDto) {
        var content: Post = postRepository.findById(editPostDto.id).get()
        content.content = editPostDto.content
        content.modifiedDate = LocalDateTime.now()
    }

    @Transactional
    fun deletePost(token: String, id: Long) {
        val emailFromToken: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(emailFromToken) ?: throw UserNotFoundException()
        postRepository.deleteByIdAndUserId(id, user.id)
    }

    @Transactional
    fun likePost(token: String, id: Long): Long {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(email) ?: throw UserNotFoundException()
        val post: Post = postRepository.findPostById(id) ?: throw ContentNotFoundException()
        if (thumbsUpRepository.findThumbsUpByUserAndPost(user, post) != null) {
            thumbsUpRepository.deleteThumbsUpByUserAndPost(user, post)
        } else {
            thumbsUpRepository.save(ThumbsUp(user = user, post = post))
        }
        if (post.user != user) {

            sendNotification(
                "게시글 알림",
                "${user.userInfo!!.nickName}님이 게시글에 좋아요를 했습니다.",
                user.userInfo!!.notificationToken!!,
                post,
                user
            )
        }

        return thumbsUpRepository.countThumbsUpsByPost(post)
    }

    @Transactional
    fun writeComment(commentCreateDto: PostDto.CommentCreateDto, token: String): Long {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(email) ?: throw UserNotFoundException()
        val post: Post = postRepository.findPostById(commentCreateDto.postId) ?: throw ContentNotFoundException()

        val comment = commentRepository.save(commentCreateDto.toComment(post, user))

        // 알림
        val userInfo: UserInfo = userInfoRepository.findByUser(post.user)!!
        if (userInfo.notificationToken != null) {
            val title = "댓글 알림"
            val message = "게시글에 새로운 댓글이 작성되었습니다."
            sendNotification(title, message, userInfo.notificationToken!!, post, post.user)


        }

        return comment.id
    }

    @Transactional
    fun deleteComment(replyId: Long, token: String) {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(email) ?: throw UserNotFoundException()

        // 토큰의 주인과 댓글의 작성자가 같다면 삭제
        val comment = commentRepository.findCommentById(replyId) ?: throw ContentNotFoundException()
        if (comment.writer!!.emailAddress != user.emailAddress) throw NotPermittedException() // exception 핸들링 예정
        commentRepository.deleteByReply(replyId)
        commentRepository.deleteById(replyId)

    }

    // 댓글 조회
    fun findPostComment(postId: Long, page: PageRequest): List<PostDto.CommentListResponseDto> {
        logger().info("댓글 조회")
        postRepository.findPostById(postId) ?: throw ContentNotFoundException()
        val commentList =
            commentRepository.findCommentByPostIdAndReplyIsNullOrderByCreatedDateAsc(postId, page)

        val ansList = mutableListOf<PostDto.CommentListResponseDto>()
        for (c in commentList) {

            val u = userInfoRepository.findByUser(c.writer!!)
            val cnt = commentRepository.countByReply(c.id)
            ansList.add(
                PostDto.CommentListResponseDto(
                    c.id,
                    c.reply,
                    c.content,
                    u!!.nickName,
                    u.user.id,
                    u.profileImg,
                    cnt,
                    Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(0)))
                )
            )

        }

        commentList.forEach { c ->

        }

        return ansList

    }

    //대댓글 조회
    fun findReplyComment(postId: Long, replyId: Long, page: PageRequest): List<PostDto.ReplyCommentListResponsedto> {
        val commentList =
            commentRepository.findCommentByPostIdAndReplyOrderByCreatedDateAsc(postId, replyId, page)

        val ansList = mutableListOf<PostDto.ReplyCommentListResponsedto>()
        for (c in commentList) {
            val u = userInfoRepository.findByUser(c.writer!!)

            ansList.add(
                PostDto.ReplyCommentListResponsedto(
                    c.id,
                    c.reply,
                    c.content,
                    u!!.nickName,
                    u.id,
                    u.profileImg,
                    Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(0)))
                )
            )
        }

        return ansList

    }

    @Transactional(readOnly = true)
    fun getImage(fileName: String): ByteArray {
        if (fileName.substring(fileName.length - 3) == "png") {
            return FileUtil.getImage(fileName)
        }
        val media = mediaRepository.findByName(fileName)
        val thumbnail = thumbnailRepository.findByMedia(media)
        return FileUtil.getImage(thumbnail.name)

    }


    @Transactional
    fun findAllAlram(token: String): List<PostDto.NotiResponseDto> {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(email) ?: throw UserNotFoundException()

        val notiList = notiRepository.findAllByUserOrderByCreatedDateDesc(user)

        val a = mutableListOf<PostDto.NotiResponseDto>()
        for (notification in notiList) {
            val postResponse = findPost(notification.post.id, token)
            a.add(
                PostDto.NotiResponseDto(
                    notification.user.id,
                    notification.message,
                    notification.createdDate!!,
                    notification.pressed,
                    postResponse
                )
            )
        }

        return a

    }

    @Transactional
    fun readNotification(token: String, postId: Long) {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findUserByEmailAddress(email) ?: throw UserNotFoundException()

        val noti = notiRepository.findByUserAndAndPostId(user, postId)
        noti.pressed = true
    }


    fun sendNotification(title: String, message: String, token: String, post: Post, user: User) {
        logger().info("알림 보냄  to ${user.emailAddress}")
        firebaseMessagingService.sendMessage(
            token,
            title, message
        )
        // 알람 내역 저장
        notiRepository.save(Noti(message, false, post, user))
    }


}
package com.pin.pinapi.core.post.service

import com.pin.pinapi.core.notification.service.FirebaseMessagingService
import com.pin.pinapi.core.post.dto.PostDto
import com.pin.pinapi.core.post.entity.*
import com.pin.pinapi.core.post.exception.ContentNotFoundException
import com.pin.pinapi.core.post.exception.MediaNotFoundException
import com.pin.pinapi.core.post.exception.NotPermittedException
import com.pin.pinapi.core.post.repository.*
import com.pin.pinapi.core.security.util.JWTUtil
import com.pin.pinapi.core.user.entity.User
import com.pin.pinapi.core.user.entity.UserInfo
import com.pin.pinapi.core.user.exception.InvalidTokenException
import com.pin.pinapi.core.user.exception.UserNotFoundException
import com.pin.pinapi.core.user.repository.UserInfoRepository
import com.pin.pinapi.core.user.repository.UserRepository
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
    private val postRepository: PostRepository,
    private val thumbnailRepository: ThumbnailRepository,
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
    private val userInfoRepository: UserInfoRepository,
    private val thumbsUpRepository: ThumbsUpRepository,
    private val commentRepository: CommentRepository,
    private val mentionRepository: MentionRepository,
    private val firebaseMessagingService: FirebaseMessagingService,
    private val notiRepository: NotiRepository,
    private val jwtUtil: JWTUtil,
//    private val fileUtil: FileUtility
) {

    // 미디어 컨텐츠(사진, 동영상)는 파일 명만 전달하고
    // 따로 반환
    // id : 사용자 id
    @Transactional(readOnly = true)
    fun findAllMapPosts(userId: String, token: String): PostDto.PostMapAllResponse {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        // subject == email 이메일이 회원 인지 check
        val user: User = userRepository.findById(subject).orElseThrow { throw UserNotFoundException() }
        val userInfo: UserInfo = userInfoRepository.findByUser(user) ?: throw UserNotFoundException() //수정 예정

        val result: MutableList<PostDto.ContentDtoResponse> = mutableListOf()
        val myContents: List<Post> = postRepository.findAllByUser(User(userId))


        val followContents: List<Post> =
            postRepository.findAllByUserAndFollowBeforeYesterday(user, LocalDateTime.now().minusHours(24))
        val contents = myContents + followContents
        if (contents.isEmpty()) throw ContentNotFoundException()

        val extList = listOf("png", "jpg", "jpeg")

        for (c in contents) {
            var fileName = ""
            val media = mediaRepository.findFirstByPost(c)
            fileName = if (media != null) media.name else userInfo.profileImg
            // 첫번째 미디어가 동영상이면 썸네일 반환
            if (media != null && !extList.contains(media.ext)) {
                val thumbnail = thumbnailRepository.findByMedia(media)
                if (thumbnail != null)
                    fileName = thumbnail.name
            }
            val date: Date = Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(9)))
            val detail = findPost(c.id, token)
            result.add(PostDto.ContentDtoResponse(c.id, c.user.email, c.lat, c.lon, fileName, date, detail))
        }

        return PostDto.PostMapAllResponse(result)
    }

    /**
     * 같은 마이페이지인데 리스트 형태(컨텐츠의 사진 전체를 보여줌)
     */
    fun findMyListAll(postMyList: PostDto.PostMyList): List<PostDto.PostMyListResponse> {
        val myList: MutableList<PostDto.PostMyListResponse> = mutableListOf()
        val posts: Page<Post> =
            postRepository.findAllByUserOrderByCreatedDateDesc(
                User(postMyList.userEmail),
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
    fun findPost(postId: Long, token: String): PostDto.PostResponse {
        val emailAddress = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        val user: User = userRepository.findById(emailAddress).orElseThrow { throw UserNotFoundException() }
        val content: Post = postRepository.findById(postId).orElseThrow { throw ContentNotFoundException() }
        val userInfo = userInfoRepository.findByUser(User(content.user.email)) ?: throw UserNotFoundException()
        val medias: List<Media>? = mediaRepository.findAllByPost(content)

        val mediaList = mutableListOf<String>()
        val thumbnailList = mutableListOf<String>()
        val extList = listOf("png", "jpg", "jpeg")

        if (medias != null) {
            for (media in medias) {
                mediaList.add(media.name)

                if (!extList.contains(media.ext)) {
                    val thumbnail = thumbnailRepository.findByMedia(media)
                    if (thumbnail != null)
                        thumbnailList.add(thumbnail.name)
                }

            }
        }
        val date: Date = Date.from(content.createdDate!!.toInstant(ZoneOffset.ofHours(0)))


        // 좋아요 수
        val thumbsUpCount = thumbsUpRepository.countThumbsUpsByPost(content)

        // 요청자가 좋아요 했는지
        val isLikedBefore: Boolean =
            thumbsUpRepository.findThumbsUpByUserAndPost(user, content) != null
        // 댓글 수
        val commentCount: Long = commentRepository.countCommentByPostAndReplyIsNull(content)

        val mentionResult = mutableListOf<PostDto.ProfileSummaryResponseDto>()
        val mentionList: List<String> = mentionRepository.findAllByPost(content).map { it.user.email }

        for (mm in mentionList) {
            val mentionUserInfo: UserInfo = userInfoRepository.findByUser(User(mm)) ?: throw UserNotFoundException()
            mentionResult.add(
                PostDto.ProfileSummaryResponseDto(
                    mentionUserInfo.userEmail,
                    mentionUserInfo.nickName,
                    mentionUserInfo.profileImg
                )
            )
        }


        return PostDto.PostResponse(
            content.id,
            userInfo.nickName,
            content.user.email,
            userInfo.profileImg,
            content.content,
            mediaList,
            thumbnailList,
            content.locationName,
            isLikedBefore,
            thumbsUpCount,
            commentCount,
            mentionResult,
            date
        )

    }

    @Transactional(readOnly = true)
    fun findAllPost(userId: String, token: String, page: Int, size: Int): List<PostDto.PostResponse> {
        // token valid check
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        // subject == email 이메일이 회원 인지 check
        val user: User = userRepository.findById(subject).orElseThrow { throw UserNotFoundException() }

        val contents: Page<Post> =
            postRepository.findAllByUserOrderByCreatedDateDesc(User(userId), PageRequest.of(page, size))
        val userInfo = userInfoRepository.findByUser(User(userId)) ?: throw UserNotFoundException()

        val result = mutableListOf<PostDto.PostResponse>()

        val extList = listOf("png", "jpg", "jpeg")

        for (c in contents) {

            val medias: List<Media>? = mediaRepository.findAllByPost(c)
            val mediaList = mutableListOf<String>()
            val thumbnailList = mutableListOf<String>()

            if (medias != null) {
                for (media in medias) {
                    mediaList.add(media.name)

                    if (!extList.contains(media.ext)) {
                        val thumbnail = thumbnailRepository.findByMedia(media)
                        if (thumbnail != null)
                            thumbnailList.add(thumbnail.name)
                    }
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
            val mentionList: List<String> = mentionRepository.findAllByPost(c).map { it.user.email }

            for (mm in mentionList) {
                val mentionUserInfo: UserInfo = userInfoRepository.findByUser(User(mm)) ?: throw UserNotFoundException()
                mentionResult.add(
                    PostDto.ProfileSummaryResponseDto(
                        mentionUserInfo.userEmail,
                        mentionUserInfo.nickName,
                        mentionUserInfo.profileImg
                    )
                )
            }

            result.add(
                PostDto.PostResponse(
                    c.id,
                    userInfo.nickName,
                    user.email,
                    userInfo.profileImg,
                    c.content,
                    mediaList,
                    thumbnailList,
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

    @Transactional
    fun create(post: PostDto.PostCreateDto, token: String) {
        val subject = jwtUtil.getSubject(token) ?: throw InvalidTokenException()
        val user: User = userRepository.findById(subject).orElseThrow { throw UserNotFoundException() }
        val userInfo: UserInfo = userInfoRepository.findByUser(user)!!
        val content: Post = post.toPost(user)
        val contentSaved: Post = postRepository.save(content)

        var videoCnt = 0
        if (post.mediaFiles != null) {
            for (media in post.mediaFiles) {
                // 컨텐츠 타입
                val ext: String = media.ext
                val name: String = media.fileName
                val size: Long = media.size
                val m: Media = post.toMedia(name, size, ext, contentSaved)
                mediaRepository.save(m)

                if (ext == "mp4") {
                    val thumbnailFile = post.thumbnailFiles!![videoCnt]
                    val thumbnail = post.toThumbnail(thumbnailFile.fileName, thumbnailFile.size, thumbnailFile.ext, m)
                    thumbnailRepository.save(thumbnail)
                    videoCnt += 1
                }

            }
        }
        // 멘션
        if (post.mention != null) {
            for (m in post.mention) {
                val u = userRepository.findById(m).orElseThrow { throw UserNotFoundException() }
                mentionRepository.save(Mention(contentSaved, u))
            }
        }

        // 알림 보내기
        val followers: List<UserInfo> = userInfoRepository.findAllByUserJoinFollowToUserOrderByNickNameAsc(user)
        for (follower in followers) {
            if (follower.notificationToken != null) {
                val title = "게시글 알림"
                val message = "팔로우 중인 ${userInfo.nickName}님이 새로운 게시글을 업로드했습니다."
                sendNotification(title, message, follower.notificationToken!!, contentSaved, follower.user!!)
            }

        }

    }

    @Transactional
    fun editPostContent(editPostDto: PostDto.EditPostDto) {
        val content: Post = postRepository.findById(editPostDto.postId).get()
        content.content = editPostDto.content
        content.modifiedDate = LocalDateTime.now()
    }

    @Transactional
    fun deletePost(token: String, id: Long) {
        val emailFromToken: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findById(emailFromToken).orElseThrow { throw UserNotFoundException() }
        postRepository.deleteByIdAndUser(id, User(user.email))
    }

    @Transactional
    fun likePost(token: String, postId: Long): Long {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findById(email).orElseThrow { throw UserNotFoundException() }
        val post: Post = postRepository.findById(postId).orElseThrow { throw ContentNotFoundException() }
        val isAlreadyLike = thumbsUpRepository.findThumbsUpByUserAndPost(user, post)
        if (isAlreadyLike != null) {
            thumbsUpRepository.deleteThumbsUpByUserAndPost(user, post)
        } else {
            thumbsUpRepository.save(ThumbsUp(user = user, post = post))
            if (post.user != user) {

                sendNotification(
                    "게시글 알림",
                    "${user.userInfo!!.nickName}님이 게시글에 좋아요를 했습니다.",
                    user.userInfo!!.notificationToken,
                    post,
                    user
                )
            }
        }


        return thumbsUpRepository.countThumbsUpsByPost(post)
    }

    @Transactional
    fun writeComment(commentCreateDto: PostDto.CommentCreateDto, token: String): Long {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findById(email).orElseThrow { throw UserNotFoundException() }
        val post: Post =
            postRepository.findById(commentCreateDto.postId).orElseThrow { throw ContentNotFoundException() }

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
        val user: User = userRepository.findById(email).orElseThrow { throw UserNotFoundException() }

        // 토큰의 주인과 댓글의 작성자가 같다면 삭제
        val comment = commentRepository.findCommentById(replyId) ?: throw ContentNotFoundException()
        if (comment.writer!!.email != user.email) throw NotPermittedException() // exception 핸들링 예정
        commentRepository.deleteByReply(replyId)
        commentRepository.deleteById(replyId)

    }

    // 댓글 조회
    fun findPostComment(postId: Long, page: PageRequest): List<PostDto.CommentListResponseDto> {
        logger().info("댓글 조회")
        postRepository.findById(postId).orElseThrow { throw ContentNotFoundException() }
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
                    u.user!!.email,
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
                    u.userEmail,
                    u.profileImg,
                    Date.from(c.createdDate!!.toInstant(ZoneOffset.ofHours(0)))
                )
            )
        }

        return ansList

    }
//  deprecated for moving s3
//    @Transactional(readOnly = true)
//    fun getImage(fileName: String): ByteArray {
//        if (fileName.substring(fileName.length - 3) == "png") {
//            return fileUtil.getImage(fileName)
//        }
//        val media = mediaRepository.findByName(fileName) ?: throw FileNotFoundException()
//        val thumbnail = thumbnailRepository.findByMedia(media) ?: throw FileNotFoundException()
//        return fileUtil.getImage(thumbnail.name)
//
//    }


    @Transactional
    fun findAllAlram(token: String): List<PostDto.NotiResponseDto> {
        val email: String = jwtUtil.getSubject(token)
        val user: User = userRepository.findById(email).orElseThrow { throw UserNotFoundException() }

        val notiList = notiRepository.findAllByUserOrderByCreatedDateDesc(user)

        val a = mutableListOf<PostDto.NotiResponseDto>()
        for (notification in notiList) {
            val postResponse = findPost(notification.post.id, token)
            a.add(
                PostDto.NotiResponseDto(
                    notification.user.email,
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
        val user: User = userRepository.findById(email).orElseThrow { throw UserNotFoundException() }

        val noti = notiRepository.findByUserAndPostId(user, postId)
        noti.pressed = true
    }


    fun sendNotification(title: String, message: String, token: String?, post: Post, user: User) {
        logger().info("알림 보냄  to ${user.email}")
        try {
            firebaseMessagingService.sendMessage(
                token!!,
                title, message
            )
            // 알람 내역 저장
            notiRepository.save(Noti(message, false, post, user))
        } catch (e: Exception) {
            logger().info("알림 보내기 실패")
        }
    }


}
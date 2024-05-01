package com.pin.pinapi.core.post

import com.pin.pinapi.core.post.dto.PostDto
import com.pin.pinapi.core.post.service.PostService
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RequestMapping("/post")
@RestController
class PostController(
    private val postService: PostService,
) {

    // 사용자 전체 포스트 조회
    // 지도에 표시
    @ApiOperation(value = "사용자 전체 포스트 정보 조회")
    @PostMapping("/all")
    fun findAllPost(
        @RequestParam userEmail: String,
        @RequestHeader("Authorization") token: String
    ): PostDto.PostMapAllResponse {
        return postService.findAllMapPosts(userEmail, token)
    }


    @ApiOperation(value = "포스트 생성")
    @PostMapping("/create", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createPost(
        @RequestBody postCreateDto: PostDto.PostCreateDto,
        @RequestHeader("Authorization") token: String
    ) {
        postService.create(postCreateDto, token)
    }
//   deprecated for moving s3
//    @ApiOperation(value = "동영상 저장")
//    @PostMapping("/v", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun videoSave(@RequestParam("video") video: MultipartFile): String {
//        return fileUtil.fileSave(video, "mov");
//    }

    @ApiOperation(value = "포스트 삭제")
    @DeleteMapping("/delete")
    fun deletePost(@RequestHeader("Authorization") token: String, @RequestParam postId: Long) {
        postService.deletePost(token, postId)
    }

    @ApiOperation(value = "포스트 수정")
    @PatchMapping("/update")
    fun editPost(editPostDto: PostDto.EditPostDto) {
        postService.editPostContent(editPostDto)
    }

    @ApiOperation(value = "특정 포스트 세부 정보 조회")
    @PostMapping("/find")
    fun findPost(@RequestParam postId: Long, @RequestHeader("Authorization") token: String): PostDto.PostResponse {
        return postService.findPost(postId, token)
    }

    @PostMapping("/find/all")
    fun findAllPost(
        @RequestParam userEmail: String,
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestHeader("Authorization") token: String
    ): List<PostDto.PostResponse> {
        return postService.findAllPost(userEmail, token, page, size)
    }

    /**
     * as moving to s3, this endpoint deprecated
     * */
//
//@ApiOperation(value = "비디오")
//@GetMapping("/streaming")
//fun streaming(
//    @RequestParam watch: String,
//    @RequestHeader headers: HttpHeaders
//): ResponseEntity<ResourceRegion> {
//    val video = fileUtil.getResourceRegion(watch, headers)
//    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).contentType(MediaType.parseMediaType("video/mp4"))
//        .body(video)
//}
//
//    @ApiOperation(value = "이미지")
//    @GetMapping("/image", produces = [MediaType.IMAGE_PNG_VALUE])
//    fun getImage(@RequestParam watch: String): ByteArray {
//        logger().info("이미지 조회 : {} ", watch)
//        return postService.getImage(watch)
//    }

    @ApiOperation("엄지척")
    @PostMapping("/like")
    fun likePost(@RequestHeader("Authorization") token: String, @RequestParam postId: Long): Long {
        return postService.likePost(token, postId)
    }


    @ApiOperation(value = "댓글 작성")
    @PostMapping("/comment/write")
    fun writeComment(
        @RequestBody commentCreateDto: PostDto.CommentCreateDto,
        @RequestHeader("Authorization") token: String
    ): Long {
        return postService.writeComment(commentCreateDto, token)
    }

    @ApiOperation(value = "댓글 삭제")
    @DeleteMapping("/comment/delete")
    fun deleteComment(@RequestParam replyId: Long, @RequestHeader("Authorization") token: String) {
        postService.deleteComment(replyId, token)
    }

    @ApiOperation(value = "댓글 조회")
    @PostMapping("/comment")
    fun findComment(
        @RequestParam postId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): List<PostDto.CommentListResponseDto> {
        return postService.findPostComment(postId, PageRequest.of(page, size))
    }

    @ApiOperation(value = "대댓글 조회")
    @PostMapping("/comment/reply")
    fun findReplyComment(
        @RequestParam postId: Long,
        @RequestParam replyId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): List<PostDto.ReplyCommentListResponsedto> {
        return postService.findReplyComment(postId, replyId, PageRequest.of(page, size))
    }

    @ApiOperation("알람 목록 조회 ")
    @PostMapping("/alram")
    fun getAlram(@RequestHeader("Authorization") token: String): List<PostDto.NotiResponseDto> {
        return postService.findAllAlram(token)
    }


    @ApiOperation("알람 목록 조회 ")
    @PostMapping("/alram/read")
    fun readAlram(
        @RequestHeader("Authorization") token: String,
        @RequestParam postId: Long
    ) {
        postService.readNotification(token, postId)
    }


}
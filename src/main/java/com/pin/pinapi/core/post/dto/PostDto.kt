package com.pin.pinapi.core.post.dto

import com.pin.pinapi.core.post.entity.Comment
import com.pin.pinapi.core.post.entity.Media
import com.pin.pinapi.core.post.entity.Post
import com.pin.pinapi.core.post.entity.Thumbnail
import com.pin.pinapi.core.user.entity.User
import java.time.LocalDateTime
import java.util.*

class PostDto {

    data class PostMapAllResponse(val contents: List<ContentDtoResponse>)


    data class ContentDtoResponse(
        val contentId: Long,
        val userEmail: String,
        val lat: Double,
        val lon: Double,
        val thumbnail: String,
        val createdDate: Date,
        val detail: PostResponse
    )

    data class PostMyList(
        val userEmail: String,
        val page: Int,
        val size: Int
    )

    data class PostMyListResponse(
        val contentId: Long,
        val photos: List<String>,
        val locationName: String,
        val date: Date
    )

    data class ProfileSummaryResponseDto(val userId: String, val nickname: String, val profileImage: String)


    data class PostResponse(
        val postId: Long,
        val nickname: String,
        val userEmail: String,
        val profileImage: String,
        val content: String,
        val mediaFiles: List<String>,
        val thumbnailFiles: List<String>,
        val locationName: String,
        val liked: Boolean,
        val likesCount: Long,
        val commentsCount: Long,
        val mention: List<ProfileSummaryResponseDto>,
        val createdDate: Date
    )

    data class PostCreateDto(
        val content: String,
        val mediaFiles: List<MediaInfo>?,
        val thumbnailFiles: List<MediaInfo>?,
        val lat: Double,
        val lon: Double,
        val locationName: String,
        val isPrivate: Boolean,
        val mention: List<String>?
    ) {
        fun toPost(user: User): Post {
            return Post(content, lat, lon, locationName, isPrivate, user)
        }


        fun toMedia(name: String, size: Long, cType: String, post: Post): Media {
            return Media(name, size, cType, post)
        }


        fun toThumbnail(fileName: String, fileSize: Long, ext: String, media: Media): Thumbnail {
            return Thumbnail(fileName, fileSize, ext, media)
        }
    }

    data class MediaInfo(val fileName: String, val size: Long, val ext: String)

    data class EditPostDto(val postId: Long, val content: String)

    data class CommentCreateDto(val postId: Long, val replyId: Long?, val content: String) {
        fun toComment(post: Post, writer: User): Comment {
            return Comment(content, replyId, post, writer)
        }
    }

    data class CommentListResponseDto(
        val commentId: Long,
        val replyId: Long?,
        val content: String,
        val writer: String?,
        val writerId: String,
        val profileImage: String,
        val replyCount: Long,
        val createdDate: Date
    )

    data class ReplyCommentListResponsedto(
        val commentId: Long,
        val replyId: Long?,
        val content: String,
        val writer: String,
        val writerId: String,
        val profileImage: String,
        val createdDate: Date
    )

    data class NotiResponseDto(
        val userId: String,
        val message: String,
        val createdDate: LocalDateTime,
        val pressed: Boolean,
        val detail: PostResponse
    )

}
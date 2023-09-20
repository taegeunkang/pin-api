package com.pin.pinapi.core.post.repository

import com.pin.pinapi.core.post.entity.Comment
import com.pin.pinapi.core.post.entity.Post
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findCommentById(commentId: Long): Comment?

    // 댓글 조회
    fun findCommentByPostIdAndReplyIsNullOrderByCreatedDateAsc(postId: Long, page: Pageable): List<Comment>

    // 대댓글 조회
    fun findCommentByPostIdAndReplyOrderByCreatedDateAsc(postId: Long, reply: Long, page: Pageable): List<Comment>

    fun countCommentByPostAndReplyIsNull(post: Post): Long

    fun countByReply(reply: Long): Long

    fun deleteByReply(reply: Long)
}
package com.pin.pinapi.core.post.exception

import com.pin.pinapi.core.user.dto.UserDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*

@RestControllerAdvice
class PostExceptionHandler {
    @ExceptionHandler(ContentNotFoundException::class)
    protected fun handleContentNotFoundException(ex: ContentNotFoundException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 500, "P01", "posts not exist"), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MediaNotFoundException::class)
    protected fun handleMediaNotFoundException(ex: MediaNotFoundException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(
            UserDto.Error(Date(), 500, "P02", "media contents not exist"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(NotPermittedException::class)
    protected fun handleNotPermittedException(ex: NotPermittedException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(
            UserDto.Error(Date(), 500, "P03", "not permitted action"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }


}
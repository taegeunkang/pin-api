package com.pin.pinapi.core.user.exception

import com.pin.pinapi.core.user.dto.UserDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*

@RestControllerAdvice
class CustomExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    protected fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 404, "U01", "user not found"), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(PasswordNotCorrectException::class)
    protected fun handlePasswordNotCorrectException(ex: PasswordNotCorrectException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U02", "password not correct"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidTokenException::class)
    protected fun handleInvalidTokenException(ex: InvalidTokenException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U03", "invalid token"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TokenNotMatchException::class)
    protected fun handleTokenNotMatchException(ex: TokenNotMatchException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U04", "tokens not match"), HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(InvalidDataException::class)
    protected fun handleInvalidDataException(ex: InvalidDataException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U05", "id, password required"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UserExistsException::class)
    protected fun handleUserExistsException(ex: UserExistsException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U06", "user exists"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NicknameExistsException::class)
    protected fun handleNicknameExistsException(ex: NicknameExistsException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U07", "nickname exists"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EmailAndTokenNotMatchException::class)
    protected fun handleEmailAndTokenNotMatchException(ex: EmailAndTokenNotMatchException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U10", "email not match with token"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConnectionErrorException::class)
    protected fun handleConnectionErrorException(ex: ConnectionErrorException?): ResponseEntity<UserDto.Error> {
        return ResponseEntity<UserDto.Error>(
            UserDto.Error(
                Date(),
                500,
                "U11",
                "error occured connectiong with provider"
            ), HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(SocialRegisteredException::class)
    protected fun handleSocialRegisteredException(ex: SocialRegisteredException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U12", "registered by social auth"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NonceEmptyException::class)
    protected fun handleNonceEmptyException(ex: NonceEmptyException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U13", "nonce required"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TokenExpiredException::class)
    protected fun handleTokenExpiredException(ex: TokenExpiredException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U08", "tokens expired"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AlreadyInitUserException::class)
    protected fun handleAlreadyInitUserException(ex: AlreadyInitUserException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U14", "user-info exists"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotInitUserException::class)
    protected fun handleNotInitUserException(ex: NotInitUserException): ResponseEntity<UserDto.Error> {
        return ResponseEntity(UserDto.Error(Date(), 400, "U15", "user not init"), HttpStatus.BAD_REQUEST)
    }

}
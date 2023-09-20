package com.pin.pinapi.core.email.exception;

import com.pin.pinapi.core.user.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class EmailCustomExceptionHandler {

    @ExceptionHandler({SendEmailFailedException.class})
    protected ResponseEntity<UserDto.Error> handleSendEmailFailedException(SendEmailFailedException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 500, "E01", "sending email failed"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({InvalidEmailException.class})
    protected ResponseEntity<UserDto.Error> handleInvalidEmailException(InvalidEmailException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E02", "invalid email"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({KeyExpiredException.class})
    protected ResponseEntity<UserDto.Error> handleInvalidKeyException(KeyExpiredException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E03", "expired key"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotVerifiedEmailException.class})
    protected ResponseEntity<UserDto.Error> handleNotVerifiedEmailException(NotVerifiedEmailException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E04", "email not verified"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AlreadyVerifiedEmailException.class})
    protected ResponseEntity<UserDto.Error> handleAlreadyVerifiedEmailException(AlreadyVerifiedEmailException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E05", "already verified email"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({EmailSentRecentlyException.class})
    protected ResponseEntity<UserDto.Error> handleEmailSentRecentlyException(EmailSentRecentlyException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E06", "email sent recently"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidCodeException.class})
    protected ResponseEntity<UserDto.Error> handleInvalidCodeException(InvalidCodeException ex) {
        return new ResponseEntity<>(new UserDto.Error(new Date(), 400, "E07", "invalid code"), HttpStatus.BAD_REQUEST);
    }

}

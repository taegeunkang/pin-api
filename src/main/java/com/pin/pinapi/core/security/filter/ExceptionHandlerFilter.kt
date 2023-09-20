package com.pin.pinapi.core.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.pin.pinapi.core.user.dto.UserDto
import com.pin.pinapi.core.user.exception.EmptyTokenException
import com.pin.pinapi.core.user.exception.InvalidTokenException
import com.pin.pinapi.core.user.exception.TokenExpiredException
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ExceptionHandlerFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (ex: InvalidTokenException) {
            setErrorResponse(Date(), HttpStatus.BAD_REQUEST, "U03", "invalid token", response)
        } catch (ex: TokenExpiredException) {
            setErrorResponse(Date(), HttpStatus.BAD_REQUEST, "U08", "token expired", response)
        } catch (ex: EmptyTokenException) {
            setErrorResponse(Date(), HttpStatus.BAD_REQUEST, "U09", "header is empty", response)
        } catch (ex: NullPointerException) {
            setErrorResponse(Date(), HttpStatus.BAD_REQUEST, "U09", "header is empty", response)
        }
    }

    private fun setErrorResponse(
        date: Date,
        httpStatus: HttpStatus,
        code: String,
        message: String,
        response: HttpServletResponse
    ) {
        response.status = httpStatus.value()
        response.contentType = "application/json"
        val errorDTO = UserDto.Error(date, httpStatus.value(), code, message)
        try {
            val objectMapper = ObjectMapper()
            response.writer.write(objectMapper.writeValueAsString(errorDTO))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
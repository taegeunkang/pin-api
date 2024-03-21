package com.pin.pinapi.core.security.filter

import com.pin.pinapi.core.user.exception.InvalidTokenException
import com.pin.pinapi.core.user.exception.TokenExpiredException
import org.springframework.web.filter.OncePerRequestFilter
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginCheckFilter(private val jwtUtil: com.pin.pinapi.core.security.util.JWTUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.info("request url : ${request.requestURI}")
        if (!checkURI(request.requestURI)) {
            val token = request.getHeader("Authorization")

            if (!jwtUtil.validToken(token)) {
                throw InvalidTokenException()
            } else if (jwtUtil.getExp(token).before(Date.from(ZonedDateTime.now().toInstant()))) {
                throw TokenExpiredException()
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun checkURI(requestURI: String): Boolean {
        if (requestURI.startsWith("/docs") || requestURI.startsWith("/swagger-ui")
            || requestURI.startsWith("/v2/api-docs") || requestURI.startsWith("/swagger-resources")
            || requestURI.startsWith("/csrf") || requestURI.startsWith("/webjars")
        ) {
            return true
        }
        for (i in WHITE_LIST.indices) {
            if (WHITE_LIST[i] == requestURI) {
                return true
            }
        }
        return false
    }

    companion object {
        private val WHITE_LIST = arrayOf(
            "/post/test",
            "/user/register",
            "/user/login",
            "/user/login/oauth",
            "/user/follower/list",
            "/user/following/list",
            "/user/profile/info",
            "/user/profile/image",
            "/",
            "/video/upload",
            "/post/v",
            "/post/streaming",
            "/post/find",
            "/email/send",
            "/email/verify",
            "/user/nickname/duplicate",
            "/user/reset/password",
            "/user/check",
            "/post/image",
            "/post/image/test",
            "/post/comment",
            "/.well-known/pki-validation/786C6C77AD8942138242595AD9DCC04E.txt"
        )
    }
}
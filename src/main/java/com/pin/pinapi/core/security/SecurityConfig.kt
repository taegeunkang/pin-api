package com.pin.pinapi.core.security

import com.pin.pinapi.core.security.filter.ExceptionHandlerFilter
import com.pin.pinapi.core.security.filter.LoginCheckFilter
import com.pin.pinapi.core.security.util.JWTUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity -> web.ignoring().anyRequest() }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtUtil(
        @Value("\${jwt.secret}") secret: String,
        @Value("\${jwt.expire}") expire: Long,
        @Value("\${jwt.refreshTokenExpire}") refreshTokenExpire: Long
    ): JWTUtil {
        return JWTUtil(secret, expire, refreshTokenExpire)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf().and().cors().disable().formLogin().disable().build()
    }

    @Bean
    fun exceptionHandlerFilter(): ExceptionHandlerFilter {
        return ExceptionHandlerFilter()
    }

    @Bean
    fun loginCheckFilter(jwtUtil: JWTUtil): LoginCheckFilter {
        return LoginCheckFilter(jwtUtil)
    }
}
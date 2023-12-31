package com.trep.trepapi.core.media.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.commons.CommonsMultipartResolver

@Configuration
class MediaConfig {
    private val FILE_MAX_UPLOAD_SIZE = 524288000 // 1024 * 1024* 100 * 5; 500메가

    @Bean
    fun multipartResolver(): MultipartResolver {
        val multipartResolver = CommonsMultipartResolver()
        multipartResolver.setMaxUploadSize(FILE_MAX_UPLOAD_SIZE.toLong())
        println("등록됨")
        return multipartResolver
    }
}
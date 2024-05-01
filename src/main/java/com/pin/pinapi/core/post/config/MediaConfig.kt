package com.trep.trepapi.core.media.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.commons.CommonsMultipartResolver

@Configuration
class MediaConfig {
    @Value("\${media.upload.max}")
    var FILE_MAX_UPLOAD_SIZE: Long? = null

    @Bean
    fun multipartResolver(): MultipartResolver {
        val multipartResolver = CommonsMultipartResolver()
        multipartResolver.setMaxUploadSize(FILE_MAX_UPLOAD_SIZE!!)
        return multipartResolver
    }
}
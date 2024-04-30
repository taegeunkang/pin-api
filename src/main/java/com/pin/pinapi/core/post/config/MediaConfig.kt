package com.trep.trepapi.core.media.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.commons.CommonsMultipartResolver

@Configuration
class MediaConfig {


    @Bean
    fun multipartResolver(@Value("\${media.upload.max}") fileMaxUploadSize: Long): MultipartResolver {
        val multipartResolver = CommonsMultipartResolver()
        multipartResolver.setMaxUploadSize(fileMaxUploadSize)
        return multipartResolver
    }
}
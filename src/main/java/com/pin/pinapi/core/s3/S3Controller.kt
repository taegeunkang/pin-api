package com.pin.pinapi.core.s3

import com.pin.pinapi.core.s3.dto.S3Dto
import com.pin.pinapi.core.s3.service.S3Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/s3")
class S3Controller(
    private val s3Service: S3Service
) {
    @PostMapping("/init")
    fun initiateUpload(@RequestBody s3UploadInitDto: S3Dto.S3UploadInitDto): S3Dto.S3UploadInitDtoResponse {
        return s3Service.initUpload(s3UploadInitDto.fileName)
    }

    @PostMapping("/upload")
    fun getUploadSignedUrl(@RequestBody s3UploadSignedUrlDto: S3Dto.S3UploadSignedUrlDto): S3Dto.S3UploadSignedUrlDtoResponse {
        return s3Service.getUploadSignedUrl(s3UploadSignedUrlDto)
    }

    @PostMapping("/complete")
    fun completeUpload(@RequestBody s3UploadCompleteDto: S3Dto.S3UploadCompleteDto): S3Dto.S3UploadResultDto {
        return s3Service.completeUpload(s3UploadCompleteDto)
    }

    @PostMapping("/abort")
    fun abortUpload(@RequestBody s3UploadAbortDto: S3Dto.S3UploadAbortDto) {
        s3Service.aboardUpload(s3UploadAbortDto)
        
    }
}
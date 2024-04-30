package com.pin.pinapi.core.s3.dto

class S3Dto {
    data class S3UploadInitDto(val fileName: String)
    data class S3UploadInitDtoResponse(val uploadId: String, val fileName: String)
    data class S3UploadSignedUrlDto(val uploadId: String, val fileName: String, val partNumber: Int)
    data class S3UploadSignedUrlDtoResponse(val requestUrl: String)
    data class S3UploadCompleteDto(val fileName: String, val parts: List<S3UploadPartsDetailDto>, val uploadId: String)
    data class S3UploadPartsDetailDto(val partNumber: Int, val awsETag: String)
    data class S3UploadResultDto(val fileName: String, val url: String)
    data class S3UploadAbortDto(val fileName: String, val uploadId: String)
}
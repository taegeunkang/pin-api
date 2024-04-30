package com.pin.pinapi.core.s3.service

import com.amazonaws.services.s3.AmazonS3Client
import com.pin.pinapi.core.s3.dto.S3Dto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val amazonS3Client: AmazonS3Client,
    @Value("\${cloud.aws.s3.bucket.image}")
    private val imageBucketName: String,
    @Value("\${cloud.aws.s3.bucket.video}")
    private val videoBucketName: String

) {

    fun initUpload(fileName: String): S3Dto.S3UploadInitDtoResponse {
        val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
        val newFileName = UUID.randomUUID().toString() + "." + ext
        val bucketName = if (ext == "png") imageBucketName else videoBucketName

        val createMultipartUploadRequest: CreateMultipartUploadRequest =
            CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(newFileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .expires(Instant.now().plusSeconds(60 * 20))
                .build()

        val s3Response: CreateMultipartUploadResponse = s3Client.createMultipartUpload(createMultipartUploadRequest)

        return S3Dto.S3UploadInitDtoResponse(s3Response.uploadId(), newFileName)

    }

    fun getUploadSignedUrl(s3UploadSignedUrlDto: S3Dto.S3UploadSignedUrlDto): S3Dto.S3UploadSignedUrlDtoResponse {
        val ext = s3UploadSignedUrlDto.fileName.substring(s3UploadSignedUrlDto.fileName.lastIndexOf(".") + 1)
        val bucketName = if (ext == "png") imageBucketName else videoBucketName

        val uploadPartRequest: UploadPartRequest =
            UploadPartRequest.builder()
                .bucket(bucketName)
                .key(s3UploadSignedUrlDto.fileName)
                .uploadId(s3UploadSignedUrlDto.uploadId)
                .partNumber(s3UploadSignedUrlDto.partNumber)
                .build()

        val uploadPartPresignRequest: UploadPartPresignRequest = UploadPartPresignRequest.builder().signatureDuration(
            Duration.ofMinutes(10)
        )
            .uploadPartRequest(uploadPartRequest)
            .build()

        val presignedUploadPartRequest: PresignedUploadPartRequest =
            s3Presigner.presignUploadPart(uploadPartPresignRequest)

        return S3Dto.S3UploadSignedUrlDtoResponse(presignedUploadPartRequest.url().toString())

    }

    fun completeUpload(s3UploadCompleteDto: S3Dto.S3UploadCompleteDto): S3Dto.S3UploadResultDto {

        val ext = s3UploadCompleteDto.fileName.substring(s3UploadCompleteDto.fileName.lastIndexOf(".") + 1)
        val bucketName = if (ext == "png") imageBucketName else videoBucketName
        val completeParts: ArrayList<CompletedPart> = ArrayList()

        for (partForm: S3Dto.S3UploadPartsDetailDto in s3UploadCompleteDto.parts) {
            val part = CompletedPart.builder().partNumber(partForm.partNumber)
                .eTag(partForm.awsETag)
                .build()

            completeParts.add(part)
        }


        val completedMultipartUpload: CompletedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(completeParts)
            .build()

        val fileName: String = s3UploadCompleteDto.fileName

        val completeMultipartUploadRequest: CompleteMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .uploadId(s3UploadCompleteDto.uploadId)
            .multipartUpload(completedMultipartUpload).build()

        val completeMultipartUpload: CompleteMultipartUploadResponse =
            s3Client.completeMultipartUpload(completeMultipartUploadRequest)

        val objectKey: String = completeMultipartUpload.key()
        val url: String = amazonS3Client.getUrl(bucketName, objectKey).toString()

        return S3Dto.S3UploadResultDto(fileName, url)

    }

    fun aboardUpload(s3UploadAbortDto: S3Dto.S3UploadAbortDto) {
        val ext = s3UploadAbortDto.fileName.substring(s3UploadAbortDto.fileName.lastIndexOf(".") + 1)
        val bucketName = if (ext == "png") imageBucketName else videoBucketName

        val abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(s3UploadAbortDto.fileName)
            .uploadId(s3UploadAbortDto.uploadId)
            .build()
        s3Client.abortMultipartUpload(abortMultipartUploadRequest)

    }
}
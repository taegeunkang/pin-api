package com.pin.pinapi.core.s3.config


import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class S3Config {
    @Value("\${cloud.aws.credentials.access-key}")
    private lateinit var ACCESS_KEY: String

    @Value("\${cloud.aws.credentials.secret-key}")
    private lateinit var SECRET_KEY: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var REGION: String

    @Bean
    fun basicAwsCredentials(): AwsCredentials {
        return AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
    }

    @Bean
    fun s3Client(awsCredentials: AwsCredentials): S3Client {
        return S3Client.builder().region(Region.of(REGION))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build()
    }

    @Bean
    fun s3Presigner(awsCredentials: AwsCredentials): S3Presigner {
        return S3Presigner.builder().region(Region.of(REGION))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build()
    }

    @Bean
    fun amazonS3Client(awsCredentials: AwsCredentials): AmazonS3Client {

        return AmazonS3ClientBuilder.standard()
            .withCredentials(DefaultAWSCredentialsProviderChain())
            .withRegion(REGION)
            .build() as AmazonS3Client

    }

}
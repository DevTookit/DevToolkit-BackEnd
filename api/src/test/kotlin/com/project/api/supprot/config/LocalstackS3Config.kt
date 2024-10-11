package com.project.api.supprot.config

import com.project.api.config.properties.S3Properties
import com.project.api.supprot.container.LocalStackContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest

@Configuration
@Profile("test")
class LocalstackS3Config(
    private val s3Properties: S3Properties,
    @Autowired private val localStackContainer: LocalStackContainer,
) {
    @Bean
    fun s3Client(): S3Client {
        localStackContainer.localStack().start()
        val s3Client =
            S3Client
                .builder()
                .endpointOverride(localStackContainer.localStack().getEndpointOverride(Service.S3))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            "accesskey",
                            "secretkey",
                        ),
                    ),
                ).build()

        val request = CreateBucketRequest.builder().bucket(s3Properties.bucketName).build()
        s3Client.createBucket(request)

        return s3Client
    }
}

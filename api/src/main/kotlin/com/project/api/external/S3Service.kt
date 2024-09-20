package com.project.api.external

import com.project.api.config.properties.CloudFrontProperties
import com.project.api.config.properties.S3Properties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3Service(
    private val s3Client: S3Client,
    private val cloudFrontProperties: CloudFrontProperties,
    private val s3Properties: S3Properties,
) {
    fun uploadFile(
        path: String,
        file: MultipartFile,
        fileName: String,
    ) {
        val request =
            PutObjectRequest
                .builder()
                .bucket(s3Properties.bucketName)
                .key("$path/$fileName")
                .contentType(file.contentType)
                .contentDisposition("inline")
                .build()
        val body = RequestBody.fromInputStream(file.inputStream, file.size)

        s3Client.putObject(request, body)
    }

    fun deleteFile(pathFileName: String) {
        val request =
            DeleteObjectRequest
                .builder()
                .bucket(s3Properties.bucketName)
                .key(pathFileName)
                .build()
        s3Client.deleteObject(request)
    }

    fun readFile(
        fileName: String,
        path: String,
    ) = "${cloudFrontProperties.url}/$path/$fileName"
}

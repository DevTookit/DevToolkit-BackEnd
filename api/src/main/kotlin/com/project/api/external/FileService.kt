package com.project.api.external

import com.project.api.external.dto.FileResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

@Service
class FileService(
    private val s3Service: S3Service,
) {
    fun upload(
        file: MultipartFile,
        path: String,
    ): FileResponse {
        val fileName = createFileName(file)
        try {
            s3Service.uploadFile(path, file, fileName)
            return FileResponse(
                url =
                    s3Service.readFile(
                        fileName = fileName,
                        path = path,
                    ),
                size = file.size,
                isSuccess = true,
            )
        } catch (e: IOException) {
            return FileResponse(
                isSuccess = false,
                errorMessage = e.localizedMessage,
            )
        }
    }

    fun delete(url: String): FileResponse {
        val index = url.indexOf("net/") + 4
        val pathFileName = url.substring(index)

        return try {
            s3Service.deleteFile(pathFileName)
            FileResponse(
                isSuccess = true,
            )
        } catch (e: IOException) {
            FileResponse(
                isSuccess = false,
                errorMessage = e.localizedMessage,
            )
        }
    }

    private fun createFileName(file: MultipartFile) =
        file.originalFilename!!.let {
            "${UUID.randomUUID()}${it.substring(it.lastIndexOf("."))}"
        }
}

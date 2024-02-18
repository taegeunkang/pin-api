package com.pin.pinapi.util

import com.pin.pinapi.util.LogUtil.logger
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRange
import org.springframework.util.FileCopyUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object FileUtil {

    val filePath: String
        get() = "/files"

    fun makeFolder(path: String) {
        val folder = File(path)
        val p = Paths.get(path)
        if (!folder.exists()) {
            Files.createDirectory(p)
        }
    }

    fun decodeBase64Image(encodedImg: String): ByteArray {
        val parts = encodedImg.split(",");
        val imageData = parts[1]
        val decodedImage = Base64.getDecoder().decode(imageData)
        return decodedImage
    }


    fun fileSave(mfile: MultipartFile, ext: String): String {
        val file: ByteArray = mfile.bytes
        val fileName = UUID.randomUUID().toString() + "." + ext
        val savePath = filePath + "/" + fileName
        makeFolder(filePath)
        val localFile = File(savePath)
        FileCopyUtils.copy(file, localFile)
        return fileName
    }


    fun getResourceRegion(file: String, headers: HttpHeaders): ResourceRegion {

        logger().info("file resource {}", filePath + "/" + file)
        val video = UrlResource("file:${filePath}/${file}")
        logger().info("url resource created")

        val chunkSize = 10000000L // 10MB;
        val contentLength = video.contentLength()
        val httpRange: HttpRange = if (headers.range.isEmpty()) {
            HttpRange.createByteRange(0)
        } else {
            headers.range.stream().findFirst().get()
        }
        logger().info("file size : {}", contentLength)

        val start = httpRange.getRangeStart(contentLength)
        val end = httpRange.getRangeEnd(contentLength)
        val rangeLength = java.lang.Long.min(chunkSize, end - start + 1)
        logger().info("range start {} end {} ", start, rangeLength)
        return ResourceRegion(video, start, rangeLength)

    }

    fun getImage(file: String): ByteArray {
        val path: String = "$filePath/$file"
        return File(path).readBytes()
    }


}
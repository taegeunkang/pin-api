package com.pin.pinapi.util

import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.HttpHeaders
import org.springframework.web.multipart.MultipartFile

class S3FileUtility : FileUtility {

    override fun makeFolder(path: String) {
        TODO("Not yet implemented")
    }

    override fun fileSave(file: MultipartFile, ext: String): String {
        TODO("Not yet implemented")
    }

    override fun getResourceRegion(file: String, headers: HttpHeaders): ResourceRegion {
        TODO("Not yet implemented")
    }

    override fun getImage(file: String): ByteArray {
        TODO("Not yet implemented")
    }
}
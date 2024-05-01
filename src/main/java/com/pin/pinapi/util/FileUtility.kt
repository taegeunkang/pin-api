package com.pin.pinapi.util

import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.HttpHeaders
import org.springframework.web.multipart.MultipartFile

interface FileUtility {

    fun makeFolder(path: String)

    fun fileSave(file: MultipartFile, ext: String): String

    fun getResourceRegion(file: String, headers: HttpHeaders): ResourceRegion
    
    fun getImage(file: String): ByteArray
}
package com.pin.pinapi.util


import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LogUtil {

    inline fun <reified T> T.logger(): Logger {
        return LoggerFactory.getLogger(T::class.java)
    }

}
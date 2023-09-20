package com.pin.pinapi.core.notification.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.pin.pinapi.util.LogUtil.logger
import org.springframework.stereotype.Service

@Service
class FirebaseMessagingService(val firebaseMessaging: FirebaseMessaging) {

    fun sendMessage(target: String, title: String, body: String) {
        try {
            val message = makeMessage(target, title, body)
            firebaseMessaging.send(message)
        } catch (ex: Exception) {
            logger().info("notification 발송 실패")
        }
    }

    private fun makeMessage(target: String, title: String, body: String): Message {
        val notification: Notification = Notification.builder().setTitle(title).setBody(body).build()
        val message: Message = Message.builder().setToken(target).setNotification(notification).build()
        return message
    }
}
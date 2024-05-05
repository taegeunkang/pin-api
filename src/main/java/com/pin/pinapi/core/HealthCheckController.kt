package com.pin.pinapi.core

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/")
@RestController
class HealthCheckController {

    @RequestMapping("/health")
    fun healthCheck(): String {
        return "health check ok"
    }
}
package com.pin.pinapi.swagger

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Controller
class SwaggerRedirector {
    @RequestMapping("/docs")
    fun api(): String {
        return "redirect:/swagger-ui.html";
    }
}
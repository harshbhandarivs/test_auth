package com.testing_auth

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Controller("/hello")
class HelloController {

    @Get(uri="/", produces=["text/plain"])
    @Secured(SecurityRule.IS_AUTHENTICATED)
    fun index(): String {
        return "Example Response"
    }
}

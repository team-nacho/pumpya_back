package com.sigma.pumpya.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController("/profile")
class ProfileController(
    private val env: Environment
) {
    @GetMapping("/Z2V0LXByb2ZpbGU")
    fun getProfile(): String {
        return Arrays.stream(env.activeProfiles).findFirst().orElse("");

    }
}
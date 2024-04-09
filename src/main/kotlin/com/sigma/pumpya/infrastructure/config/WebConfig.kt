package com.sigma.pumpya.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig() : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") //모든 요청에 대해서 CORS 설정
            // 권한 조정 필요. 허용할 origin만 설정해야하는데 일단 다 허용
            .allowedOrigins("*")
    }
}
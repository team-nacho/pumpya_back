package com.sigma.pumpya.application

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sigma.pumpya.domain.entity.Member
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

//
@Service
class MemberService (
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper : ObjectMapper
){

}
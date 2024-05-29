package com.sigma.pumpya.domain.entity

import com.sigma.pumpya.infrastructure.EpochTimeUtil.getCurrentEpochTime
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseTimeEntity(
    @CreatedDate
    @field:Column(name = "created_at")
    var createdAt: Long? = null,

    @LastModifiedDate
    @field:Column(name = "updated_at")
    var updatedAt: Long? = null
) {
    @PrePersist
    fun onCreate() {
        createdAt = getCurrentEpochTime()
        updatedAt = createdAt
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = getCurrentEpochTime()
    }
}
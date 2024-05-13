package com.sigma.pumpya.infrastructure.repository

import com.sigma.pumpya.domain.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, String> {

}
package com.sigma.pumpya.infrastructure.repository

import com.sigma.pumpya.domain.entity.Party
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PartyRepository : JpaRepository<Party, String> { }

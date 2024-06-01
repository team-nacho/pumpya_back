package com.sigma.pumpya.infrastructure.repository

import com.sigma.pumpya.domain.entity.Currency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyRepository : JpaRepository<Currency, String> {}
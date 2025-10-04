package com.aryan.expensemanager.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

data class CountryResponse(
    val name: CountryName,
    val currencies: Map<String, CurrencyInfo>?
)

data class CountryName(
    val common: String,
    val official: String
)

data class CurrencyInfo(
    val name: String,
    val symbol: String
)

data class ExchangeRateResponse(
    val base: String,
    val rates: Map<String, Double>
)

interface CountryApi {
    @GET("v3.1/all?fields=name,currencies")
    suspend fun getAllCountries(): List<CountryResponse>
}

interface ExchangeRateApi {
    @GET("v4/latest/{base}")
    suspend fun getExchangeRates(@Path("base") base: String): ExchangeRateResponse
}
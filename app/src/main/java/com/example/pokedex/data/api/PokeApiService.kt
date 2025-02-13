package com.example.pokedex.data.api

import com.example.pokedex.data.model.*
import retrofit2.http.*

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 1000, // Obtén todos los Pokémon
        @Query("offset") offset: Int = 0
    ): PokemonResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailResponse
}
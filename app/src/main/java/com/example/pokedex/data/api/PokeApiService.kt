package com.example.pokedex.data.api

import com.example.pokedex.data.model.PokemonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 1000, // Obtén todos los Pokémon
        @Query("offset") offset: Int = 0
    ): PokemonResponse
}
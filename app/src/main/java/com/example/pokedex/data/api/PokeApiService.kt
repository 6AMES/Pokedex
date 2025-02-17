package com.example.pokedex.data.api

import com.example.pokedex.data.model.*
import retrofit2.http.*

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int, // Número de Pokémon por página
        @Query("offset") offset: Int // Desplazamiento para la paginación
    ): PokemonResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailResponse

    @GET("type")
    suspend fun getPokemonTypes(): PokemonTypeResponse

    @GET("generation")
    suspend fun getPokemonGenerations(): PokemonGenerationResponse

    @GET("version/{id}")
    suspend fun getVersionDetail(@Path("id") id: Int): VersionDetailResponse
}
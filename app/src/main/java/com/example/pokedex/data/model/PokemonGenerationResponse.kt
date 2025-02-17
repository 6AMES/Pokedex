package com.example.pokedex.data.model

data class PokemonGenerationResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Generation>
)

data class Generation(
    val name: String,
    val url: String
)
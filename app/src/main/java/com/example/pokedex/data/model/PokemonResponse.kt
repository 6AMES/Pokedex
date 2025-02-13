package com.example.pokedex.data.model

data class PokemonResponse(
    val results: List<PokemonApi>
)

data class PokemonApi(
    val name: String,
    val url: String
)
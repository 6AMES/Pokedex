package com.example.pokedex.data.model

data class PokemonTypeResponse(
    val results: List<PokemonTypeApi>
)

data class PokemonTypeApi(
    val name: String,
    val url: String
)
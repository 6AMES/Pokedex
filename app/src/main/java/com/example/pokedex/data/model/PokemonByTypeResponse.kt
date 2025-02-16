package com.example.pokedex.data.model

data class PokemonByTypeResponse(
    val pokemon: List<PokemonTypeSlot>
)

data class PokemonTypeSlot(
    val pokemon: PokemonApi
)

data class PokemonApi(
    val name: String,
    val url: String
)
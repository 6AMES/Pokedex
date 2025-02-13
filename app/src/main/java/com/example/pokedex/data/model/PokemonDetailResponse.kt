package com.example.pokedex.data.model

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val sprites: PokemonSprites
)

data class PokemonType(
    val type: PokemonTypeDetail
)

data class PokemonTypeDetail(
    val name: String
)

data class PokemonSprites(
    val front_default: String // URL de la imagen del Pokémon
)
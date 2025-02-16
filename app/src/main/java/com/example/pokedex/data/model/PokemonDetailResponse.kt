package com.example.pokedex.data.model

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val sprites: PokemonSprites,
    val stats: List<PokemonStat>,
    val weight: Int,
    val height: Int
)

data class PokemonType(
    val slot: Int,
    val type: PokemonTypeDetail
)

data class PokemonTypeDetail(
    val name: String,
    val url: String
)

data class PokemonSprites(
    val front_default: String?
)

data class PokemonStat(
    val base_stat: Int,
    val effort: Int,
    val stat: PokemonStatDetail
)

data class PokemonStatDetail(
    val name: String,
    val url: String
)
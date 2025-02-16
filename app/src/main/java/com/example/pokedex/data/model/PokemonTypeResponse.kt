package com.example.pokedex.data.model

data class PokemonTypeResponse(
    val results: List<PokemonTypeApi> // Cambia "PokemonType" a "PokemonTypeApi"
)

data class PokemonTypeApi( // Renombra la clase
    val name: String,
    val url: String
)
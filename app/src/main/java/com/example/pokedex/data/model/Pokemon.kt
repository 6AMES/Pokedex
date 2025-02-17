package com.example.pokedex.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val generation: String,
    val game: String,
    val imageUrl: String,
    var isFavorite: Boolean = false,
    var isCaptured: Boolean = false
)
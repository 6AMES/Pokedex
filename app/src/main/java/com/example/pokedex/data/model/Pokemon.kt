package com.example.pokedex.data.model

data class Pokemon(
    val name: String,
    val isFavorite: Boolean = false,
    val isCaptured: Boolean = false
)
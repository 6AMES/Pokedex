package com.example.pokedex.model

data class Pokemon(
    val name: String,
    val isFavorite: Boolean = false,
    val isCaptured: Boolean = false
)
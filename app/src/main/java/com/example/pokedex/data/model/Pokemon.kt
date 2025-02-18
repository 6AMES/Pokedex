package com.example.pokedex.data.model

import android.os.Parcel
import android.os.Parcelable

// Modelo principal: Pokémon
data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val generation: String,
    val imageUrl: String,
    var isFavorite: Boolean = false,
    var isCaptured: Boolean = false,
    val weight: Int? = null, // Peso en hectogramos
    val height: Int? = null, // Altura en decímetros
    val stats: Map<String, Int>? = null // Estadísticas del Pokémon
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readHashMap(Int::class.java.classLoader) as? Map<String, Int>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeStringList(types)
        parcel.writeString(generation)
        parcel.writeString(imageUrl)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeByte(if (isCaptured) 1 else 0)
        parcel.writeValue(weight)
        parcel.writeValue(height)
        parcel.writeMap(stats)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pokemon> {
        override fun createFromParcel(parcel: Parcel): Pokemon {
            return Pokemon(parcel)
        }

        override fun newArray(size: Int): Array<Pokemon?> {
            return arrayOfNulls(size)
        }
    }
}

// Modelo para tipos de Pokémon
data class PokemonType(
    val slot: Int,
    val type: PokemonTypeDetail
)

data class PokemonTypeDetail(
    val name: String,
    val url: String
)

// Modelo para sprites de Pokémon
data class PokemonSprites(
    val front_default: String?
)

// Modelo para estadísticas de Pokémon
data class PokemonStat(
    val base_stat: Int,
    val effort: Int,
    val stat: PokemonStatDetail
)

data class PokemonStatDetail(
    val name: String,
    val url: String
)

// Modelo para respuesta detallada de la PokeAPI
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val sprites: PokemonSprites,
    val stats: List<PokemonStat>,
    val weight: Int,
    val height: Int
)
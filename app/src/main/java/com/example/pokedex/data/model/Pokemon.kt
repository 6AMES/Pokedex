package com.example.pokedex.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Modelo principal: Pokémon
data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val generation: String,
    val imageUrl: String,
    var isFavorite: Boolean = false,
    var isCaptured: Boolean = false,
    val weight: Int? = null,
    val height: Int? = null,
    val stats: Map<String, Int>? = null,
    var evolutions: List<String>? = null
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
    val height: Int,
    @SerializedName("cries") val cries: Cries?
)

// Modelo para la respuesta de especie de Pokémon
data class PokemonSpeciesResponse(
    val evolution_chain: EvolutionChainUrl,
    val flavor_text_entries: List<FlavorTextEntry>,
    val genera: List<Genus>,
)

data class EvolutionChainUrl(
    val url: String
)

// Modelo para la respuesta de la cadena de evolución
data class EvolutionChainResponse(
    val chain: EvolutionChainNode
)

data class EvolutionChainNode(
    val species: EvolutionSpecies,
    val evolves_to: List<EvolutionChainNode>
)

data class EvolutionSpecies(
    val name: String,
    val url: String
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: Language,
    val version: Version
)

data class Language(
    val name: String,
    val url: String
)

data class Version(
    val name: String,
    val url: String
)

data class Genus(
    val genus: String,
    val language: Language
)

data class Cries(
    val latest: String?,
    val legacy: String?
)
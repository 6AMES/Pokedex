package com.example.pokedex.data.model

import com.google.gson.annotations.SerializedName

data class VersionDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("names") val names: List<Name>,
    @SerializedName("version_group") val versionGroup: VersionGroup
)

data class Name(
    @SerializedName("name") val name: String,
    @SerializedName("language") val language: Language
)

data class Language(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class VersionGroup(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

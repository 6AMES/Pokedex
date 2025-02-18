package com.example.pokedex.ui.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pokedex.data.model.Pokemon
import com.example.pokedex.databinding.ActivityPokemonDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PokemonDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el Pokémon pasado desde el intent
        val pokemon = intent.getParcelableExtra<Pokemon>("POKEMON_EXTRA")
        if (pokemon != null) {
            displayPokemonDetails(pokemon)
            setupFavoriteButton(pokemon)
            setupCapturedButton(pokemon)
        }
    }

    private fun displayPokemonDetails(pokemon: Pokemon) {
        // Asignar el nombre del Pokémon
        binding.pokemonName.text = pokemon.name.capitalize()

        // Cargar la imagen del Pokémon usando Glide
        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(binding.pokemonImage)

        // Asignar los tipos del Pokémon
        binding.pokemonTypes.text = "Types: ${pokemon.types.joinToString(", ")}"

        // Asignar la generación del Pokémon
        binding.pokemonGeneration.text = "Generation: ${pokemon.generation}"

        // Convertir el peso de hectogramos a kilogramos
        val weightInKg = pokemon.weight?.div(10.0) ?: 0
        binding.pokemonWeight.text = "Weight: ${weightInKg} kg"

        // Convertir la altura de decímetros a metros
        val heightInMeters = pokemon.height?.div(10.0) ?: 0
        binding.pokemonHeight.text = "Height: ${heightInMeters} m"

        // Asignar las estadísticas del Pokémon
        binding.pokemonHp.text = "HP: ${pokemon.stats?.get("hp") ?: "N/A"}"
        binding.pokemonAttack.text = "Attack: ${pokemon.stats?.get("attack") ?: "N/A"}"
        binding.pokemonDefense.text = "Defense: ${pokemon.stats?.get("defense") ?: "N/A"}"
        binding.pokemonSpecialAttack.text = "Special Attack: ${pokemon.stats?.get("special-attack") ?: "N/A"}"
        binding.pokemonSpecialDefense.text = "Special Defense: ${pokemon.stats?.get("special-defense") ?: "N/A"}"
        binding.pokemonSpeed.text = "Speed: ${pokemon.stats?.get("speed") ?: "N/A"}"

        // Marcar si el Pokémon es favorito
        binding.pokemonFavorite.isChecked = pokemon.isFavorite

        // Marcar si el Pokémon ha sido capturado
        binding.pokemonCaptured.isChecked = pokemon.isCaptured
    }

    private fun setupFavoriteButton(pokemon: Pokemon) {
        binding.pokemonFavorite.setOnClickListener {
            toggleFavorite(pokemon)
        }
    }

    private fun setupCapturedButton(pokemon: Pokemon) {
        binding.pokemonCaptured.setOnClickListener {
            toggleCaptured(pokemon)
        }
    }

    private fun toggleFavorite(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        val updateData = if (pokemon.isFavorite) {
            mapOf("favorites" to FieldValue.arrayRemove(pokemon.id))
        } else {
            mapOf("favorites" to FieldValue.arrayUnion(pokemon.id))
        }

        userRef.update(updateData).addOnSuccessListener {
            pokemon.isFavorite = !pokemon.isFavorite

            // Enviar el resultado al fragmento
            val resultIntent = Intent()
            resultIntent.putExtra("UPDATED_POKEMON_ID", pokemon.id)
            resultIntent.putExtra("IS_FAVORITE", pokemon.isFavorite)
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }

    private fun toggleCaptured(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        val updateData = if (pokemon.isCaptured) {
            mapOf("captured" to FieldValue.arrayRemove(pokemon.id))
        } else {
            mapOf("captured" to FieldValue.arrayUnion(pokemon.id))
        }

        userRef.update(updateData).addOnSuccessListener {
            pokemon.isCaptured = !pokemon.isCaptured

            // Enviar el resultado al fragmento
            val resultIntent = Intent()
            resultIntent.putExtra("UPDATED_POKEMON_ID", pokemon.id)
            resultIntent.putExtra("IS_CAPTURED", pokemon.isCaptured)
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }
}
package com.example.pokedex.ui.detail

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.data.api.RetrofitInstance
import com.example.pokedex.data.model.EvolutionChainNode
import com.example.pokedex.data.model.Pokemon
import com.example.pokedex.databinding.ActivityPokemonDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.IOException

class PokemonDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent puede contener un Pokémon completo o solo el ID
        val pokemon = intent.getParcelableExtra<Pokemon>("POKEMON_EXTRA")
        val pokemonId = intent.getIntExtra("POKEMON_ID", -1)

        if (pokemon != null) {
            // Si se recibe un objeto Pokémon, mostrarlo directamente
            displayPokemonDetails(pokemon)
            setupFavoriteButton(pokemon)
            setupCapturedButton(pokemon)
        } else if (pokemonId != -1) {
            lifecycleScope.launch {
                try {
                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemonId)

                    // Verificar datos en Logcat
                    Log.d("PokemonDetailActivity", "PokemonDetailResponse: $pokemonDetail")

                    // Convertir PokemonDetailResponse a Pokemon
                    val newPokemon = Pokemon(
                        id = pokemonDetail.id,
                        name = pokemonDetail.name.capitalize(),
                        types = pokemonDetail.types.map { it.type.name.capitalize() },
                        generation = "Desconocida",
                        imageUrl = pokemonDetail.sprites.front_default
                            ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemonDetail.id}.png",
                        weight = pokemonDetail.weight ?: 0,
                        height = pokemonDetail.height ?: 0,
                        stats = pokemonDetail.stats.associate { it.stat.name to it.base_stat }
                    )

                    // Consultar Firebase para obtener el estado de favoritos y capturados
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Convertir la lista de favoritos y capturados de Long a Int
                            val favorites = (document.get("favorites") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
                            val captured = (document.get("captured") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
                            val updatedPokemon = newPokemon.copy(
                                isFavorite = favorites.contains(pokemonId),
                                isCaptured = captured.contains(pokemonId)
                            )
                            runOnUiThread {
                                displayPokemonDetails(updatedPokemon)
                                setupFavoriteButton(updatedPokemon)
                                setupCapturedButton(updatedPokemon)
                            }
                        } else {
                            runOnUiThread {
                                displayPokemonDetails(newPokemon)
                                setupFavoriteButton(newPokemon)
                                setupCapturedButton(newPokemon)
                            }
                        }
                    }.addOnFailureListener {
                        runOnUiThread {
                            displayPokemonDetails(newPokemon)
                            setupFavoriteButton(newPokemon)
                            setupCapturedButton(newPokemon)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("PokemonDetailActivity", "Error al obtener detalles del Pokémon: ${e.message}")
                }
            }
        }
    }

    private fun displayPokemonDetails(pokemon: Pokemon) {
        // Asignar el nombre del Pokémon
        binding.pokemonId.text = "#${pokemon.id}"
        binding.pokemonName.text = pokemon.name.capitalize()

        // Cargar la imagen del Pokémon usando Glide
        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(binding.pokemonImage)

        // Asignar los tipos del Pokémon y aplicar los colores
        val typeColors = mapOf(
            "FIRE" to R.color.type_fire,
            "WATER" to R.color.type_water,
            "GRASS" to R.color.type_grass,
            "ELECTRIC" to R.color.type_electric,
            "ICE" to R.color.type_ice,
            "FIGHTING" to R.color.type_fighting,
            "POISON" to R.color.type_poison,
            "GROUND" to R.color.type_ground,
            "FLYING" to R.color.type_flying,
            "PSYCHIC" to R.color.type_psychic,
            "BUG" to R.color.type_bug,
            "ROCK" to R.color.type_rock,
            "GHOST" to R.color.type_ghost,
            "DRAGON" to R.color.type_dragon,
            "DARK" to R.color.type_dark,
            "STEEL" to R.color.type_steel,
            "FAIRY" to R.color.type_fairy,
            "NORMAL" to R.color.type_normal
        )

        val context = this

        window.statusBarColor = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)

        val backgroundColor = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)
        binding.scrollView.setBackgroundColor(backgroundColor)

        // Primer tipo
        if (pokemon.types.isNotEmpty()) {
            binding.type1TextView.text = pokemon.types[0].uppercase() // Mostrar el primer tipo
            val color1 = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)

            val bg1 = GradientDrawable().apply {
                setColor(color1)  // Color de fondo
                setCornerRadius(16f)  // Bordes redondeados
                setStroke(4, color1)  // Borde del mismo color
            }
            binding.type1TextView.background = bg1
            binding.type1TextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Color del texto

            binding.type1TextView.visibility = View.VISIBLE
        } else {
            binding.type1TextView.visibility = View.GONE
        }

        // Segundo tipo
        if (pokemon.types.size > 1) {
            binding.type2TextView.text = pokemon.types[1].uppercase() // Mostrar el segundo tipo
            val color2 = ContextCompat.getColor(context, typeColors[pokemon.types[1].uppercase()] ?: R.color.black)

            val bg2 = GradientDrawable().apply {
                setColor(color2)
                setCornerRadius(16f)
                setStroke(4, color2)
            }
            binding.type2TextView.background = bg2
            binding.type2TextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Color del texto

            binding.type2TextView.visibility = View.VISIBLE
        } else {
            binding.type2TextView.visibility = View.GONE
        }

        // Convertir el peso de hectogramos a kilogramos
        val weightInKg = pokemon.weight?.div(10.0) ?: 0
        binding.pokemonWeight.text = "${weightInKg} Kg"

        // Convertir la altura de decímetros a metros
        val heightInMeters = pokemon.height?.div(10.0) ?: 0
        binding.pokemonHeight.text = "${heightInMeters} m"

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE

        // Establecer el color de fondo
        val colorOriginal = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)
        val colorOscuro = ColorUtils.blendARGB(colorOriginal, Color.BLACK, 0.2f)

        drawable.setColor(colorOscuro)

        // Definir el corner radius solo en las esquinas superiores e inferiores de la izquierda
        drawable.cornerRadii = floatArrayOf(
            16f, 16f, // Arriba izquierda
            0f, 0f, // Arriba derecha
            0f, 0f, // Abajo derecha
            16f, 16f // Abajo izquierda
        )

        // Asignar el drawable al fondo del TextView
        binding.pokemonHp2.background = drawable
        binding.pokemonAttack2.background = drawable
        binding.pokemonDefense2.background = drawable
        binding.pokemonSpecialAttack2.background = drawable
        binding.pokemonSpecialDefense2.background = drawable
        binding.pokemonSpeed2.background = drawable

        val drawable2 = GradientDrawable()
        drawable2.shape = GradientDrawable.RECTANGLE

        // Establecer el color de fondo
        drawable2.setColor(ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black))

        // Definir el corner radius solo en las esquinas superiores e inferiores de la izquierda
        drawable2.cornerRadii = floatArrayOf(
            0f, 0f,  // Arriba izquierda
            16f, 16f,    // Arriba derecha
            16f, 16f,  // Abajo derecha
            0f, 0f     // Abajo izquierda
        )

        // Asignar el drawable al fondo del TextView
        binding.pokemonHp.background = drawable2
        binding.pokemonAttack.background = drawable2
        binding.pokemonDefense.background = drawable2
        binding.pokemonSpecialAttack.background = drawable2
        binding.pokemonSpecialDefense.background = drawable2
        binding.pokemonSpeed.background = drawable2

        // Asignar las estadísticas del Pokémon
        binding.pokemonHp.text = "${pokemon.stats?.get("hp") ?: "N/A"}"
        binding.pokemonAttack.text = "${pokemon.stats?.get("attack") ?: "N/A"}"
        binding.pokemonDefense.text = "${pokemon.stats?.get("defense") ?: "N/A"}"
        binding.pokemonSpecialAttack.text = "${pokemon.stats?.get("special-attack") ?: "N/A"}"
        binding.pokemonSpecialDefense.text = "${pokemon.stats?.get("special-defense") ?: "N/A"}"
        binding.pokemonSpeed.text = "${pokemon.stats?.get("speed") ?: "N/A"}"

        // En tu código donde obtienes los valores para los TextViews:
        val hpValue = pokemon.stats?.get("hp") ?: 0
        val attackValue = pokemon.stats?.get("attack") ?: 0
        val defenseValue = pokemon.stats?.get("defense") ?: 0
        val specialAttackValue = pokemon.stats?.get("special-attack") ?: 0
        val specialDefenseValue = pokemon.stats?.get("special-defense") ?: 0
        val speedValue = pokemon.stats?.get("speed") ?: 0

        val totalStats = hpValue + attackValue + defenseValue + specialAttackValue + specialDefenseValue + speedValue

        // Crear el texto con diferentes colores
        val totalText = "TOTAL $totalStats"
        val spannable = SpannableString(totalText)

        // Color para el número
        val colorTotal = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)

        // Aplicar color solo al número total
        spannable.setSpan(
            ForegroundColorSpan(colorTotal),
            totalText.indexOf("$totalStats"), // Encuentra dónde empieza el número
            totalText.length, // Hasta el final del texto
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Asignar el texto estilizado al TextView
        binding.sumatoriyText.text = spannable

        // Ajustar el ancho de cada TextView basado en el valor
        adjustTextViewWidth(binding.pokemonHp, hpValue)
        adjustTextViewWidth(binding.pokemonAttack, attackValue)
        adjustTextViewWidth(binding.pokemonDefense, defenseValue)
        adjustTextViewWidth(binding.pokemonSpecialAttack, specialAttackValue)
        adjustTextViewWidth(binding.pokemonSpecialDefense, specialDefenseValue)
        adjustTextViewWidth(binding.pokemonSpeed, speedValue)

        val favoriteIcon = if (pokemon.isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
        binding.pokemonFavorite.setImageResource(favoriteIcon)

        val capturedIcon = if (pokemon.isCaptured) R.drawable.ic_captured_selected else R.drawable.ic_captured_unselected
        binding.pokemonCaptured.setImageResource(capturedIcon)

        binding.pokemonFavorite.isSelected = pokemon.isFavorite
        binding.pokemonCaptured.isSelected = pokemon.isCaptured

        // Cargar las evoluciones del Pokémon
        loadEvolutions(pokemon.id)

        // Cargar la descripción de la Pokédex, habilidades, sonido y descripción de la especie
        loadAdditionalDetails(pokemon.id)
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
            updateFavoriteButtonState(pokemon)
            savePokemonState(pokemon)
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
            updateCapturedButtonState(pokemon)
            savePokemonState(pokemon)
        }
    }

    private fun updateFavoriteButtonState(pokemon: Pokemon) {
        val favoriteIcon = if (pokemon.isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
        binding.pokemonFavorite.setImageResource(favoriteIcon)
    }

    private fun updateCapturedButtonState(pokemon: Pokemon) {
        val capturedIcon = if (pokemon.isCaptured) R.drawable.ic_captured_selected else R.drawable.ic_captured_unselected
        binding.pokemonCaptured.setImageResource(capturedIcon)
    }

    private fun savePokemonState(pokemon: Pokemon) {
        val resultIntent = Intent().apply {
            putExtra("UPDATED_POKEMON_ID", pokemon.id)
            putExtra("IS_FAVORITE", pokemon.isFavorite)
            putExtra("IS_CAPTURED", pokemon.isCaptured)
        }
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun loadEvolutions(pokemonId: Int) {
        lifecycleScope.launch {
            try {
                // Obtener la especie del Pokémon
                val speciesResponse = RetrofitInstance.api.getPokemonSpecies(pokemonId)
                val evolutionChainUrl = speciesResponse.evolution_chain.url

                // Extraer el ID de la cadena de evolución
                val evolutionChainId = evolutionChainUrl.split("/").filter { it.isNotEmpty() }.last().toInt()

                // Obtener la cadena de evolución
                val evolutionResponse = RetrofitInstance.api.getEvolutionChain(evolutionChainId)

                // Extraer la lista de evoluciones
                val evolutionList = mutableListOf<String>()
                var currentEvolution: EvolutionChainNode? = evolutionResponse.chain

                while (currentEvolution != null) {
                    evolutionList.add(currentEvolution.species.name)
                    currentEvolution = currentEvolution.evolves_to.firstOrNull() // Tomar solo la primera evolución
                }

                runOnUiThread {
                    // Verifica cuántas evoluciones hay en la lista y asigna cada una a un TextView
                    binding.pokemonEvolutions1.text = evolutionList.getOrNull(0)?.capitalize() ?: ""
                    binding.pokemonEvolutions2.text = evolutionList.getOrNull(1)?.capitalize() ?: ""
                    binding.pokemonEvolutions3.text = evolutionList.getOrNull(2)?.capitalize() ?: ""

                    // Ocultar los TextView de evoluciones si no hay suficientes
                    if (evolutionList.size < 3) {
                        binding.pokemonEvolutions3.visibility = View.GONE // Ocultar la tercera evolución si no existe
                    } else {
                        binding.pokemonEvolutions3.visibility = View.VISIBLE // Mostrar si existe
                    }

                    if (evolutionList.size < 2) {
                        binding.pokemonEvolutions2.visibility = View.GONE // Ocultar la segunda evolución si no existe
                    } else {
                        binding.pokemonEvolutions2.visibility = View.VISIBLE // Mostrar si existe
                    }
                }

                runOnUiThread {
                    val evolutionCards = listOf(
                        binding.evolutionCard1 to evolutionList.getOrNull(0),
                        binding.evolutionCard2 to evolutionList.getOrNull(1),
                        binding.evolutionCard3 to evolutionList.getOrNull(2)
                    )

                    for ((cardView, evolutionName) in evolutionCards) {
                        if (evolutionName != null) {
                            cardView.visibility = View.VISIBLE // Asegurar que el CardView sea visible
                            cardView.setOnClickListener {
                                navigateToPokemonDetail(evolutionName)
                            }
                        } else {
                            cardView.visibility = View.GONE // Ocultar si no hay evolución
                        }
                    }
                }

                // Cargar imágenes de las evoluciones
                loadEvolutionImages(evolutionList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadEvolutionImages(pokemonNames: List<String>) {
        lifecycleScope.launch {
            val images = pokemonNames.mapNotNull { name ->
                try {
                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(name)
                    pokemonDetail.sprites.front_default
                } catch (e: Exception) {
                    null
                }
            }

            runOnUiThread {
                // Mostrar imágenes en ImageViews
                Glide.with(this@PokemonDetailActivity).load(images.getOrNull(0)).into(binding.evolutionImage1)
                Glide.with(this@PokemonDetailActivity).load(images.getOrNull(1)).into(binding.evolutionImage2)
                Glide.with(this@PokemonDetailActivity).load(images.getOrNull(2)).into(binding.evolutionImage3)
            }
        }
    }

    private fun loadAdditionalDetails(pokemonId: Int) {
        lifecycleScope.launch {
            try {
                // Obtener la especie del Pokémon
                val speciesResponse = RetrofitInstance.api.getPokemonSpecies(pokemonId)
                Log.d("PokemonDetailActivity", "Species Response: $speciesResponse")

                // Obtener la descripción de la Pokédex (en español)
                val pokedexDescription = speciesResponse.flavor_text_entries
                    .firstOrNull { it.language.name == "es" }?.flavor_text
                    ?.replace("\n", " ")
                    ?: "No hay descripción disponible."

                // Obtener la descripción de la especie (en español)
                val speciesDescription = speciesResponse.genera
                    .firstOrNull { it.language.name == "es" }?.genus
                    ?: "No hay descripción de especie."

                // Ahora obtenemos los datos desde la URL correcta
                val pokemonResponse = RetrofitInstance.api.getPokemonDetail(pokemonId)
                val pokemonSoundUrl = pokemonResponse.cries?.latest

                Log.d("PokemonDetailActivity", "Pokemon Sound URL: $pokemonSoundUrl")

                runOnUiThread {
                    binding.pokemonDescription.text = pokedexDescription
                    binding.pokemonSpeciesDescription.text = speciesDescription

                    // Configurar el botón de sonido
                    if (pokemonSoundUrl != null) {
                        binding.pokemonSoundButton.setOnClickListener {
                            playPokemonSound(pokemonSoundUrl)
                        }
                    } else {
                        binding.pokemonSoundButton.isEnabled = false
                        binding.pokemonSoundButton.text = "Sin sonido"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PokemonDetailActivity", "Error cargando detalles adicionales: ${e.message}")
            }
        }
    }

    private fun playPokemonSound(soundUrl: String) {
        Log.d("PokemonDetailActivity", "Intentando reproducir sonido desde URL: $soundUrl")
        mediaPlayer?.release() // Liberar el MediaPlayer anterior si existe

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(soundUrl)
                prepareAsync() // Preparar de forma asíncrona
                setOnPreparedListener {
                    Log.d("PokemonDetailActivity", "MediaPlayer preparado, iniciando reproducción")
                    start() // Reproducir el sonido cuando esté listo
                }
                setOnCompletionListener {
                    release() // Liberar recursos después de la reproducción
                }
            } catch (e: IOException) {
                Log.e("PokemonDetailActivity", "Error al reproducir sonido: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Liberar el MediaPlayer cuando la actividad se destruya
    }

    private fun adjustTextViewWidth(textView: TextView, value: Int) {
        val layoutParams = textView.layoutParams
        // Si el valor es mayor o igual a 100, asignamos el ancho máximo (por ejemplo, MATCH_PARENT)
        layoutParams.width = if (value >= 100) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            // Ancho en función del valor (puedes ajustar el valor multiplicado para obtener el efecto que desees)
            val width = (value / 100f * 500f).toInt()  // Ajusta el 2ºf a lo que mejor se ajuste
            width
        }
        textView.layoutParams = layoutParams
    }

    private fun navigateToPokemonDetail(pokemonName: String) {
        lifecycleScope.launch {
            try {
                val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemonName)
                val pokemonId = pokemonDetail.id

                val intent = Intent(this@PokemonDetailActivity, PokemonDetailActivity::class.java)
                intent.putExtra("POKEMON_ID", pokemonId) // Pasamos el ID
                startActivity(intent)

            } catch (e: Exception) {
                Log.e("PokemonDetailActivity", "Error al obtener datos de $pokemonName: ${e.message}")
            }
        }
    }
}
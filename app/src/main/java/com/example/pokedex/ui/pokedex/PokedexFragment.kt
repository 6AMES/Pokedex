package com.example.pokedex.ui.pokedex

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.example.pokedex.R
import com.example.pokedex.data.api.RetrofitInstance
import com.example.pokedex.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch

class PokedexFragment : Fragment() {

    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()
    private val filteredList = mutableListOf<Pokemon>()
    private var currentFilter: FilterType = FilterType.ALL
    private var currentPage = 0
    private val pageSize = 25
    private var isLoading = false
    private var firstLoad = true

    private var selectedType: String? = null
    private var selectedGeneration: String? = null

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val favorites = mutableSetOf<Int>()
    private val captured = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pokedex, container, false)

        adapter = PokemonAdapter(
            filteredList, // Usamos la lista filtrada directamente
            onFavoriteClick = { pokemon -> toggleFavorite(pokemon) },
            onCapturedClick = { pokemon -> toggleCaptured(pokemon) }
        )

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Cargamos los favoritos y capturados primero, luego los Pokémon
        loadFavoritesAndCaptured {
            // Ahora cargamos los Pokémon sin esperar al scroll
            loadPokemonList()
        }
    }

    // Carga los favoritos y capturados ANTES de cargar Pokémon
    private fun loadFavoritesAndCaptured(onComplete: () -> Unit) {
        if (userId.isEmpty()) return

        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            favorites.clear()
            captured.clear()

            favorites.addAll((document.get("favorites") as? List<*>)?.mapNotNull { it.toString().toIntOrNull() } ?: emptyList())
            captured.addAll((document.get("captured") as? List<*>)?.mapNotNull { it.toString().toIntOrNull() } ?: emptyList())

            Log.d("PokedexFragment", "Favoritos cargados: $favorites")
            Log.d("PokedexFragment", "Capturados cargados: $captured")

            onComplete()
        }
    }

    // Cargar la lista de Pokémon en bloques de 25 al principio
    fun loadPokemonList(resetList: Boolean = false, onComplete: (() -> Unit)? = null) {
        if (resetList) {
            pokemonList.clear()
            currentPage = 0
            firstLoad = true // Reinicia la bandera cuando se reinicia la lista
        }

        if (isLoading) return
        isLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (firstLoad) showLoadingIndicator(true)

                val response = RetrofitInstance.api.getPokemonList(limit = pageSize, offset = currentPage * pageSize)
                val pokemonApiList = response.results
                val newPokemonList = mutableListOf<Pokemon>()

                pokemonApiList.forEach { pokemonApi ->
                    val id = pokemonApi.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                    Log.d("PokedexFragment", "Cargando detalles del Pokémon con ID: $id")

                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(id)
                    val imageUrl = pokemonDetail.sprites.front_default ?: "https://example.com/default-image.png"

                    // Calcular la generación para este Pokémon específico
                    val generation = getGenerationFromPokemonId(id)

                    // Obtener el primer juego en el que aparece el Pokémon (puedes ajustar esta lógica según tus necesidades)
                    val game = getGameFromPokemonId(id)

                    val pokemon = Pokemon(
                        id = id,
                        name = pokemonDetail.name,
                        types = pokemonDetail.types.map { it.type.name },
                        generation = generation,
                        game = game, // Asignar el juego aquí
                        imageUrl = imageUrl,
                        isFavorite = favorites.contains(id),
                        isCaptured = captured.contains(id)
                    )
                    newPokemonList.add(pokemon)
                }

                // Agrega los nuevos Pokémon a la lista principal sin borrar nada
                pokemonList.addAll(newPokemonList)
                Log.d("PokedexFragment", "Se han agregado ${newPokemonList.size} Pokémon a pokemonList.")

                // Vuelve a aplicar el filtro para actualizar la lista en pantalla
                applyFilter(currentFilter)

                currentPage++
                adapter.notifyDataSetChanged()

                Log.d("PokedexFragment", "Paginación completada. Página actual: $currentPage")

            } catch (e: Exception) {
                Log.e("PokedexFragment", "Error al cargar los Pokémon: ${e.message}")
            } finally {
                if (firstLoad) {
                    showLoadingIndicator(false)
                    firstLoad = false
                }
                isLoading = false

                // Llamamos a la función de finalización si existe
                onComplete?.invoke()

                // Siempre continuar la carga hasta llegar al total (por ejemplo, 1025 Pokémon)
                if (currentPage * pageSize < 1025) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        loadPokemonList()
                    }
                }
            }
        }
    }

    // Aplica el filtro a la lista de Pokémon
    fun applyFilter(filterType: FilterType, type: String? = null, generation: String? = null, game: String? = null) {
        // Actualizar el filtro actual
        currentFilter = filterType

        // Actualizar el tipo seleccionado (si se proporciona)
        if (type != null) {
            selectedType = if (type == "all") null else type
        }

        // Actualizar la generación seleccionada (si se proporciona)
        if (generation != null) {
            selectedGeneration = if (generation == "all") null else generation
        }

        // Actualizar el juego seleccionado (si se proporciona)
        if (game != null) {
            selectedGame = if (game == "all") null else game
        }

        Log.d("PokedexFragment", "Aplicando filtro: $filterType con tipo: $selectedType, generación: $selectedGeneration y juego: $selectedGame")
        filteredList.clear()

        // Almacenar los valores actuales en variables locales
        val currentType = selectedType
        val currentGeneration = selectedGeneration
        val currentGame = selectedGame

        // Aplicar los filtros
        filteredList.addAll(
            pokemonList.filter { pokemon ->
                // Filtro por tipo (si hay un tipo seleccionado)
                val matchesType = currentType == null || pokemon.types.any { it.lowercase() == currentType.lowercase() }

                // Filtro por generación (si hay una generación seleccionada)
                val matchesGeneration = currentGeneration == null || pokemon.generation == currentGeneration

                // Filtro por juego (si hay un juego seleccionado)
                val matchesGame = currentGame == null || pokemon.game == currentGame

                // Filtro por favoritos (si el filtro activo es FAVORITES)
                val matchesFavorites = currentFilter != FilterType.FAVORITES || pokemon.isFavorite

                // Filtro por capturados (si el filtro activo es CAPTURED)
                val matchesCaptured = currentFilter != FilterType.CAPTURED || pokemon.isCaptured

                // Aplicar todos los filtros
                matchesType && matchesGeneration && matchesGame && matchesFavorites && matchesCaptured
            }
        )

        Log.d("PokedexFragment", "Filtrados ${filteredList.size} Pokémon.")
        adapter.updateList(filteredList)
    }

    private fun toggleFavorite(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        val updateData: Map<String, Any> = if (pokemon.isFavorite) {
            mapOf("favorites" to FieldValue.arrayRemove(pokemon.id))
        } else {
            mapOf("favorites" to FieldValue.arrayUnion(pokemon.id))
        }

        userRef.update(updateData).addOnSuccessListener {
            pokemon.isFavorite = !pokemon.isFavorite
            if (pokemon.isFavorite) favorites.add(pokemon.id) else favorites.remove(pokemon.id)
            adapter.notifyItemChanged(pokemonList.indexOf(pokemon))
        }
    }

    private fun toggleCaptured(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        val updateData: Map<String, Any> = if (pokemon.isCaptured) {
            mapOf("captured" to FieldValue.arrayRemove(pokemon.id))
        } else {
            mapOf("captured" to FieldValue.arrayUnion(pokemon.id))
        }

        userRef.update(updateData).addOnSuccessListener {
            pokemon.isCaptured = !pokemon.isCaptured
            if (pokemon.isCaptured) captured.add(pokemon.id) else captured.remove(pokemon.id)
            adapter.notifyItemChanged(pokemonList.indexOf(pokemon))
        }
    }

    private fun showLoadingIndicator(show: Boolean) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getGenerationFromPokemonId(id: Int): String {
        return when (id) {
            in 1..151 -> "generation-i"
            in 152..251 -> "generation-ii"
            in 252..386 -> "generation-iii"
            in 387..493 -> "generation-iv"
            in 494..649 -> "generation-v"
            in 650..721 -> "generation-vi"
            in 722..809 -> "generation-vii"
            in 810..905 -> "generation-viii"
            else -> "generation-ix"
        }
    }

    private fun getGameFromPokemonId(id: Int): String {
        return when (id) {
            in 1..151 -> listOf("red", "blue", "yellow").random() // Primera generación
            in 152..251 -> listOf("gold", "silver", "crystal").random() // Segunda generación
            in 252..386 -> listOf("ruby", "sapphire", "emerald").random() // Tercera generación
            in 387..493 -> listOf("diamond", "pearl", "platinum").random() // Cuarta generación
            in 494..649 -> listOf("black", "white").random() // Quinta generación
            in 650..721 -> listOf("x", "y").random() // Sexta generación
            in 722..809 -> listOf("sun", "moon", "ultra_sun", "ultra_moon").random() // Séptima generación
            in 810..905 -> listOf("sword", "shield").random() // Octava generación
            in 906..1025 -> listOf("scarlet", "violet").random() // Novena generación
            else -> "unknown" // Para Pokémon fuera de los rangos conocidos
        }
    }

}
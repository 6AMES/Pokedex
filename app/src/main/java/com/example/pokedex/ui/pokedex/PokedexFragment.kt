package com.example.pokedex.ui.pokedex

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.api.RetrofitInstance
import com.example.pokedex.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class PokedexFragment : Fragment() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedPokemonId = data?.getIntExtra("UPDATED_POKEMON_ID", -1)
                val isFavorite = data?.getBooleanExtra("IS_FAVORITE", false)
                val isCaptured = data?.getBooleanExtra("IS_CAPTURED", false)

                if (updatedPokemonId != null && updatedPokemonId != -1) {
                    val pokemonToUpdate = pokemonList.find { it.id == updatedPokemonId }
                    pokemonToUpdate?.let {
                        if (isFavorite != null) it.isFavorite = isFavorite
                        if (isCaptured != null) it.isCaptured = isCaptured
                        adapter.notifyItemChanged(pokemonList.indexOf(it))
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pokedex, container, false)

        adapter = PokemonAdapter(
            filteredList,
            onFavoriteClick = { pokemon -> toggleFavorite(pokemon) },
            onCapturedClick = { pokemon -> toggleCaptured(pokemon) },
            activityResultLauncher = activityResultLauncher
        )

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedPokemonId = data?.getIntExtra("UPDATED_POKEMON_ID", -1)
                val isFavorite = data?.getBooleanExtra("IS_FAVORITE", false)
                val isCaptured = data?.getBooleanExtra("IS_CAPTURED", false)

                if (updatedPokemonId != null && updatedPokemonId != -1) {
                    // Actualizar el Pokémon en la lista
                    val pokemonToUpdate = pokemonList.find { it.id == updatedPokemonId }
                    pokemonToUpdate?.let {
                        if (isFavorite != null) it.isFavorite = isFavorite
                        if (isCaptured != null) it.isCaptured = isCaptured

                        // Notificar al adaptador que los datos han cambiado
                        adapter.notifyItemChanged(pokemonList.indexOf(it))
                    }
                }
            }
        }

        // Configurar el RecyclerView y el adaptador aquí
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PokemonAdapter(
            filteredList, // Usamos la lista filtrada directamente
            onFavoriteClick = { pokemon -> toggleFavorite(pokemon) },
            onCapturedClick = { pokemon -> toggleCaptured(pokemon) },
            activityResultLauncher = activityResultLauncher // Pasar el ActivityResultLauncher
        )
        recyclerView.adapter = adapter

        // Cargar los favoritos y capturados primero, luego los Pokémon
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

                    // Obtener detalles del Pokémon desde la PokeAPI
                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(id)
                    val imageUrl = pokemonDetail.sprites.front_default ?: "https://example.com/default-image.png"

                    // Extraer peso y altura
                    val weight = pokemonDetail.weight
                    val height = pokemonDetail.height

                    // Extraer estadísticas
                    val stats = pokemonDetail.stats.associate { stat: PokemonStat ->
                        stat.stat.name to stat.base_stat
                    }

                    // Extraer tipos
                    val types = pokemonDetail.types.map { type: PokemonType ->
                        type.type.name
                    }

                    // Calcular la generación para este Pokémon específico
                    val generation = getGenerationFromPokemonId(id)

                    // Crear el objeto Pokémon
                    val pokemon = Pokemon(
                        id = id,
                        name = pokemonDetail.name,
                        types = types,
                        generation = generation,
                        imageUrl = imageUrl,
                        isFavorite = favorites.contains(id),
                        isCaptured = captured.contains(id),
                        weight = weight, // Añadir peso
                        height = height, // Añadir altura
                        stats = stats // Añadir estadísticas
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
    fun applyFilter(
        filterType: FilterType,
        type: String? = null,
        generation: String? = null
    ) {
        // Actualizamos el filtro global (por ejemplo, para favoritos o capturados)
        currentFilter = filterType

        when (filterType) {
            // Si se selecciona ALL, queremos reiniciar ambos filtros,
            // salvo que se provea un valor explícito para alguno.
            FilterType.ALL -> {
                // Si no se provee ningún parámetro, reiniciamos ambos.
                if (type == null && generation == null) {
                    selectedType = null
                    selectedGeneration = null
                } else {
                    // Si se ha proporcionado alguno, actualizamos ese filtro.
                    type?.let { selectedType = if (it.lowercase() == "all") null else it }
                    generation?.let { selectedGeneration = if (it.lowercase() == "all") null else it }
                }
            }
            // Si se está actualizando solo el filtro de tipo...
            FilterType.TYPE -> {
                type?.let { selectedType = if (it.lowercase() == "all") null else it }
                // Dejamos selectedGeneration sin modificar.
            }
            // Si se actualiza solo el filtro de generación...
            FilterType.GENERATION -> {
                generation?.let { selectedGeneration = if (it.lowercase() == "all") null else it }
                // Dejamos selectedType sin modificar.
            }
            // Para otros filtros (como FAVORITES o CAPTURED) no modificamos tipo/generación.
            else -> {
                // Aquí podrías decidir si forzar o no actualizar según se requiera.
            }
        }

        Log.d(
            "PokedexFragment",
            "Aplicando filtro: $filterType con tipo: $selectedType y generación: $selectedGeneration"
        )
        filteredList.clear()

        // Guardamos en variables locales para la filtración.
        val currentType = selectedType
        val currentGeneration = selectedGeneration

        filteredList.addAll(
            pokemonList.filter { pokemon ->
                val matchesType = currentType == null ||
                        pokemon.types.any { it.lowercase() == currentType.lowercase() }
                val matchesGeneration = currentGeneration == null ||
                        pokemon.generation == currentGeneration
                val matchesFavorites = currentFilter != FilterType.FAVORITES || pokemon.isFavorite
                val matchesCaptured = currentFilter != FilterType.CAPTURED || pokemon.isCaptured

                matchesType && matchesGeneration && matchesFavorites && matchesCaptured
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
}
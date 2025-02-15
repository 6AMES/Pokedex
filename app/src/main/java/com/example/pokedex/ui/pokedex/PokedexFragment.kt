package com.example.pokedex.ui.pokedex

import android.os.Bundle
import android.util.Log
import android.view.*
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
    private fun loadPokemonList() {
        if (isLoading) return
        isLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Cargamos los Pokémon en bloques de 25, desde el primer bloque
                val response = RetrofitInstance.api.getPokemonList(limit = pageSize, offset = currentPage * pageSize)
                val pokemonApiList = response.results
                val newPokemonList = mutableListOf<Pokemon>()

                pokemonApiList.forEach { pokemonApi ->
                    val id = pokemonApi.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(id)

                    val pokemon = Pokemon(
                        id = id,
                        name = pokemonDetail.name,
                        types = pokemonDetail.types.map { it.type.name },
                        imageUrl = pokemonDetail.sprites.front_default,
                        isFavorite = favorites.contains(id),
                        isCaptured = captured.contains(id)
                    )

                    newPokemonList.add(pokemon)
                }

                pokemonList.addAll(newPokemonList)
                applyFilter(currentFilter) // Aplica el filtro sobre todos los Pokémon cargados
                adapter.notifyDataSetChanged() // Actualiza la vista

            } catch (e: Exception) {
                Log.e("PokedexFragment", "Error al cargar los Pokémon: ${e.message}")
            } finally {
                isLoading = false
                currentPage++ // Aumenta la página para la próxima carga
                // Después de cargar, si es necesario, puedes cargar más Pokémon
                loadPokemonList() // Llama nuevamente a la función para seguir cargando en bloques de 25
            }
        }
    }

    // Aplica el filtro a la lista de Pokémon
    fun applyFilter(filterType: FilterType) {
        currentFilter = filterType
        filteredList.clear() // Limpiamos la lista filtrada

        // Dependiendo del filtro, agregamos los Pokémon correspondientes
        filteredList.addAll(
            when (filterType) {
                FilterType.ALL -> pokemonList // Todos los Pokémon
                FilterType.FAVORITES -> pokemonList.filter { it.isFavorite } // Solo los favoritos
                FilterType.CAPTURED -> pokemonList.filter { it.isCaptured } // Solo los capturados
            }
        )

        adapter.updateList(filteredList) // Actualiza la lista filtrada
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
}
package com.example.pokedex.ui.pokedex

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.api.RetrofitInstance
import com.example.pokedex.data.model.FilterType
import com.example.pokedex.data.model.Pokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class PokedexFragment : Fragment() {

    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()
    private var currentFilter: FilterType = FilterType.ALL
    private var currentPage = 0
    private val pageSize = 20 // Número de Pokémon por página
    private var isLoading = false // Para evitar múltiples cargas simultáneas

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla la vista del fragmento
        val rootView = inflater.inflate(R.layout.fragment_pokedex, container, false)

        // Inicializa el adaptador
        adapter = PokemonAdapter(
            pokemonList,
            onFavoriteClick = { pokemon ->
                toggleFavorite(pokemon)
                adapter.notifyItemChanged(pokemonList.indexOf(pokemon)) // Actualiza la vista
            },
            onCapturedClick = { pokemon ->
                toggleCaptured(pokemon)
                adapter.notifyItemChanged(pokemonList.indexOf(pokemon)) // Actualiza la vista
            }
        )

        // Llama a loadPokemonList después de inflar la vista
        loadPokemonList()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llama a setupRecyclerView después de que la vista haya sido creada
        setupRecyclerView() // Ya no es necesario pasar parámetros
    }

    private fun setupRecyclerView() {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter // Asigna el adaptador

        // Agrega un ScrollListener para la paginación
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Verifica si el usuario llegó al final de la lista
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    loadPokemonList() // Carga más Pokémon
                }
            }
        })
    }

    private fun loadPokemonList() {
        Log.d("PokedexFragment", "loadPokemonList() llamada")

        if (currentFilter != FilterType.ALL) return

        if (isLoading) return
        isLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("PokedexFragment", "Cargando Pokémon...")

                val response = RetrofitInstance.api.getPokemonList(
                    limit = pageSize,
                    offset = currentPage * pageSize
                )
                val pokemonApiList = response.results

                Log.d("PokedexFragment", "Recibido Pokémon: ${pokemonApiList.size}")

                val newPokemonList = mutableListOf<Pokemon>()

                pokemonApiList.forEach { pokemonApi ->
                    val id = pokemonApi.url.split("/").filter { it.isNotEmpty() }.last().toInt()

                    val existingPokemon = pokemonList.find { it.id == id }

                    val pokemonDetail = RetrofitInstance.api.getPokemonDetail(id)
                    val pokemon = Pokemon(
                        id = pokemonDetail.id,
                        name = pokemonDetail.name,
                        types = pokemonDetail.types.map { it.type.name },
                        imageUrl = pokemonDetail.sprites.front_default,
                        isFavorite = existingPokemon?.isFavorite ?: false,
                        isCaptured = existingPokemon?.isCaptured ?: false
                    )

                    newPokemonList.add(pokemon)
                }

                Log.d("PokedexFragment", "Cantidad de Pokémon procesados: ${newPokemonList.size}")

                if (newPokemonList.isNotEmpty()) {
                    adapter.updateList(newPokemonList)
                    currentPage++
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PokedexFragment", "Error al cargar los Pokémon: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Función para aplicar filtros
    fun applyFilter(filterType: FilterType) {
        currentFilter = filterType

        val filteredList = when (filterType) {
            FilterType.ALL -> pokemonList
            FilterType.FAVORITES -> pokemonList.filter { it.isFavorite }
            FilterType.CAPTURED -> pokemonList.filter { it.isCaptured }
        }

        adapter.updateList(filteredList)
    }

    // Función para guardar el estado de favorito en Firebase
    private fun toggleFavorite(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? MutableList<Int> ?: mutableListOf()

            if (pokemon.isFavorite) {
                favorites.add(pokemon.id)
            } else {
                favorites.remove(pokemon.id)
            }

            userRef.update("favorites", favorites)
        }
    }

    // Función para guardar el estado de capturado en Firebase
    private fun toggleCaptured(pokemon: Pokemon) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val captured = document.get("captured") as? MutableList<Int> ?: mutableListOf()

            if (pokemon.isCaptured) {
                captured.add(pokemon.id)
            } else {
                captured.remove(pokemon.id)
            }

            userRef.update("captured", captured)
        }
    }
}
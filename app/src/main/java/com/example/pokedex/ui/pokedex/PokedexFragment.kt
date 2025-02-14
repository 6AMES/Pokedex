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
import kotlinx.coroutines.tasks.await

class PokedexFragment : Fragment() {

    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()
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
            pokemonList,
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

        // 1️⃣ Primero cargamos favoritos y capturados, luego los Pokémon
        loadFavoritesAndCaptured {
            loadPokemonList()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 1) {
                    currentPage++
                    loadPokemonList()
                }
            }
        })
    }

    // 2️⃣ Carga los favoritos y capturados ANTES de cargar Pokémon
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

    // 3️⃣ Cargar la lista de Pokémon después de Firebase
    private fun loadPokemonList() {
        if (isLoading) return
        isLoading = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
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
                        isFavorite = favorites.contains(id),  // Ahora sí se asignan correctamente
                        isCaptured = captured.contains(id)
                    )

                    newPokemonList.add(pokemon)
                }

                pokemonList.addAll(newPokemonList)
                applyFilter(currentFilter)
                adapter.notifyDataSetChanged()  // Refresca la lista completa

            } catch (e: Exception) {
                Log.e("PokedexFragment", "Error al cargar los Pokémon: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Función para aplicar el filtro actual a la lista
    fun applyFilter(filterType: FilterType) {
        currentFilter = filterType
        val filteredList = pokemonList.filter { pokemon ->
            when (currentFilter) {
                FilterType.ALL -> true
                FilterType.FAVORITES -> pokemon.isFavorite
                FilterType.CAPTURED -> pokemon.isCaptured
            }
        }

        adapter.updateList(filteredList) // Asegúrate de que tu adaptador tenga esta función
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

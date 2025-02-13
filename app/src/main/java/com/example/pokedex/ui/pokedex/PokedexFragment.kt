package com.example.pokedex.ui.pokedex

import android.os.Bundle
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
import kotlinx.coroutines.launch

class PokedexFragment : Fragment() {

    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()
    private var currentFilter: FilterType = FilterType.ALL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout del fragmento
        val view = inflater.inflate(R.layout.fragment_pokedex, container, false)

        // Configura el RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = PokemonAdapter(pokemonList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Carga la lista de Pokémon desde la API
        loadPokemonList()

        return view
    }

    // Función para aplicar filtros
    fun applyFilter(filterType: FilterType) {
        currentFilter = filterType
        updatePokemonList()
    }

    // Función para actualizar la lista de Pokémon según el filtro
    private fun updatePokemonList() {
        val filteredList = when (currentFilter) {
            FilterType.ALL -> pokemonList
            FilterType.FAVORITES -> pokemonList.filter { it.isFavorite }
            FilterType.CAPTURED -> pokemonList.filter { it.isCaptured }
        }
        adapter.updateList(filteredList)
    }

    // Función para cargar la lista de Pokémon desde la API
    private fun loadPokemonList() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtén la lista de Pokémon desde la API
                val response = RetrofitInstance.api.getPokemonList()
                val pokemonApiList = response.results

                // Convierte la lista de la API a tu modelo de Pokémon
                val pokemonListFromApi = pokemonApiList.map { pokemonApi ->
                    Pokemon(
                        name = pokemonApi.name,
                        isFavorite = false, // Por defecto, no es favorito
                        isCaptured = false // Por defecto, no está capturado
                    )
                }

                // Actualiza la lista de Pokémon
                pokemonList.addAll(pokemonListFromApi)
                updatePokemonList()
            } catch (e: Exception) {
                // Maneja errores (por ejemplo, muestra un mensaje al usuario)
                e.printStackTrace()
            }
        }
    }
}
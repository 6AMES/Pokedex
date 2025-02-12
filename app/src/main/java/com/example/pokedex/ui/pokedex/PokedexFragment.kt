package com.example.pokedex.ui.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.model.FilterType
import com.example.pokedex.model.Pokemon

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

        // Carga la lista de Pokémon
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

    // Función para cargar la lista de Pokémon (simulada)
    private fun loadPokemonList() {
        // Aquí cargas la lista de Pokémon desde la API o cualquier otra fuente
        // Por ahora, usamos una lista simulada
        pokemonList.addAll(
            listOf(
                Pokemon("Pikachu", isFavorite = true, isCaptured = false),
                Pokemon("Bulbasaur", isFavorite = false, isCaptured = true),
                Pokemon("Charmander", isFavorite = true, isCaptured = true),
                Pokemon("Squirtle", isFavorite = false, isCaptured = false),
                Pokemon("Eevee", isFavorite = true, isCaptured = true)
            )
        )
        updatePokemonList()
    }
}
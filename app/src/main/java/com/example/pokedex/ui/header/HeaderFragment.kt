package com.example.pokedex.ui.header

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.api.RetrofitInstance
import com.example.pokedex.data.model.FilterType
import com.example.pokedex.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class HeaderFragment : Fragment() {
    private var currentFilter: FilterType = FilterType.ALL

    // Colores
    private lateinit var defaultTint: ColorStateList
    private lateinit var favoriteActiveTint: ColorStateList
    private lateinit var capturedActiveTint: ColorStateList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        // Configura el botón del menú lateral
        view.findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        // Configura los botones de favoritos
        val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)
        favoriteButton.setOnClickListener {
            if (currentFilter == FilterType.FAVORITES) {
                // Si el filtro activo es FAVORITES, muestra todos los Pokémon
                (activity as MainActivity).applyFilter(FilterType.ALL)
                currentFilter = FilterType.ALL  // Restablece el filtro a "todos"
                favoriteButton.imageTintList = defaultTint
            } else {
                // Aplica el filtro FAVORITES
                (activity as MainActivity).applyFilter(FilterType.FAVORITES)
                currentFilter = FilterType.FAVORITES
                favoriteButton.imageTintList = favoriteActiveTint

                // Restablece el tint del botón de "capturados"
                val capturedButton = view.findViewById<ImageButton>(R.id.capturedButton)
                capturedButton.imageTintList = defaultTint
            }
        }

        // Configura los botones de capturados
        val capturedButton = view.findViewById<ImageButton>(R.id.capturedButton)
        capturedButton.setOnClickListener {
            if (currentFilter == FilterType.CAPTURED) {
                // Si el filtro activo es CAPTURED, muestra todos los Pokémon
                (activity as MainActivity).applyFilter(FilterType.ALL)
                currentFilter = FilterType.ALL  // Restablece el filtro a "todos"
                capturedButton.imageTintList = defaultTint
            } else {
                // Aplica el filtro CAPTURED
                (activity as MainActivity).applyFilter(FilterType.CAPTURED)
                currentFilter = FilterType.CAPTURED
                capturedButton.imageTintList = capturedActiveTint

                // Restablece el tint del botón de "favoritos"
                val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)
                favoriteButton.imageTintList = defaultTint
            }
        }

        // Configura el botón de filtro por tipo
        val typeButton = view.findViewById<MaterialButton>(R.id.typeButton)
        typeButton.setOnClickListener {
            showTypeFilterBottomSheet()
        }

        // CONFIGURA EL BOTÓN DE FILTRO POR GENERACIÓN
        val generationButton = view.findViewById<Button>(R.id.generationButton)
        generationButton.setOnClickListener {
            showGenerationFilterBottomSheet()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ahora que el fragmento está asociado con el contexto, podemos inicializar los colores
        defaultTint = ContextCompat.getColorStateList(requireContext(), R.color.light_gray)!!
        favoriteActiveTint = ContextCompat.getColorStateList(requireContext(), R.color.favorite_active_color)!!
        capturedActiveTint = ContextCompat.getColorStateList(requireContext(), R.color.captured_active_color)!!

        // Establece el tint predeterminado en los botones
        val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)
        favoriteButton.imageTintList = defaultTint

        val capturedButton = view.findViewById<ImageButton>(R.id.capturedButton)
        capturedButton.imageTintList = defaultTint
    }

    // Dentro de showTypeFilterBottomSheet()
    private fun showTypeFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_type_filter, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        viewLifecycleOwner.lifecycleScope.launch {
            // Carga los tipos de Pokémon y excluye "stellar" y "unknown"
            val apiTypes = loadPokemonTypes()
            // Agrega "all" al principio para representar TODOS LOS TIPOS
            val types = listOf("all") + apiTypes
            Log.d("HeaderFragment", "Tipos cargados: $types")

            val typeAdapter = TypeAdapter(types) { selectedType ->
                val mainActivity = activity as MainActivity
                if (selectedType == "all") {
                    mainActivity.applyFilter(FilterType.ALL)
                } else {
                    mainActivity.applyFilter(FilterType.TYPE, selectedType)
                }

                val generationButton = view?.findViewById<MaterialButton>(R.id.generationButton)
                generationButton?.setOnClickListener {
                    showGenerationFilterBottomSheet()
                }

                // Actualizamos el botón de filtro (typeButton)
                val typeButton = requireView().findViewById<Button>(R.id.typeButton)

                if (selectedType == "all") {
                    typeButton.text = "TODOS LOS TIPOS"
                    val defaultColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                    typeButton.setBackgroundColor(defaultColor)
                    // *** NUEVO: Actualizamos el botón de generación a "TODAS LAS GENERACIONES"
                    val generationBtn = requireView().findViewById<Button>(R.id.generationButton)
                    generationBtn.text = "TODAS LAS GENERACIONES"
                    generationBtn.setBackgroundColor(defaultColor)
                } else {
                    typeButton.text = selectedType.uppercase()

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

                    val colorResId = typeColors[selectedType.uppercase()] ?: R.color.black
                    val color = ContextCompat.getColor(requireContext(), colorResId)
                    typeButton.setBackgroundColor(color)
                }

                bottomSheetDialog.dismiss()
            }

            val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.typeRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = typeAdapter
        }
    }

    // Función para cargar los tipos de Pokémon
    private suspend fun loadPokemonTypes(): List<String> {
        return try {
            val response = RetrofitInstance.api.getPokemonTypes()
            val types = response.results.map { it.name } // Obtén los nombres de los tipos

            // Filtra la lista para excluir "stellar" y "unknown"
            types.filter { it.lowercase() !in listOf("stellar", "unknown") }
        } catch (e: Exception) {
            Log.e("HeaderFragment", "Error al cargar los tipos de Pokémon: ${e.message}", e)
            emptyList()
        }
    }

    // Dentro de showGenerationFilterBottomSheet()
    private fun showGenerationFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_type_filter, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        viewLifecycleOwner.lifecycleScope.launch {
            // Carga las generaciones desde la API
            val apiGenerations = loadPokemonGenerations()
            // Agrega "all" al principio para representar TODAS LAS GENERACIONES
            val generations = listOf("all") + apiGenerations
            Log.d("HeaderFragment", "Generaciones cargadas: $generations")

            val generationAdapter = GenerationAdapter(generations) { selectedGeneration ->
                val mainActivity = activity as MainActivity

                if (selectedGeneration == "all") {
                    mainActivity.applyFilter(FilterType.ALL)
                } else {
                    mainActivity.applyFilter(FilterType.GENERATION, generation = selectedGeneration)
                }

                // Actualizamos el botón de filtro (generationButton)
                val generationButton = requireView().findViewById<Button>(R.id.generationButton)
                generationButton.text = if (selectedGeneration == "all") "TODAS LAS GENERACIONES" else selectedGeneration.uppercase()

                // Si se selecciona "all" en generación, también actualizamos el botón de tipos
                if (selectedGeneration == "all") {
                    val typeButton = requireView().findViewById<Button>(R.id.typeButton)
                    typeButton.text = "TODOS LOS TIPOS"
                    val defaultColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                    typeButton.setBackgroundColor(defaultColor)
                }

                bottomSheetDialog.dismiss()
            }

            val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.typeRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = generationAdapter
        }
    }

    private suspend fun loadPokemonGenerations(): List<String> {
        return try {
            val response = RetrofitInstance.api.getPokemonGenerations()
            response.results.map { it.name } // Obtén los nombres de las generaciones
        } catch (e: Exception) {
            Log.e("HeaderFragment", "Error al cargar las generaciones de Pokémon: ${e.message}", e)
            emptyList()
        }
    }
}
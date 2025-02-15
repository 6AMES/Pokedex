package com.example.pokedex.ui.header

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pokedex.R
import com.example.pokedex.data.model.FilterType
import com.example.pokedex.ui.main.MainActivity

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

        // Configura los botones de favoritos y capturados
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
}

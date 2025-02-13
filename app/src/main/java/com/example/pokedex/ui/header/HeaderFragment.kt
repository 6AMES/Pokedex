package com.example.pokedex.ui.header

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pokedex.R
import com.example.pokedex.data.model.FilterType
import com.example.pokedex.ui.main.MainActivity

class HeaderFragment : Fragment() {
    private var currentFilter: FilterType = FilterType.ALL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        // Configura el botón del menú lateral
        view.findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        // Configura el botón de favoritos
        view.findViewById<ImageButton>(R.id.favoriteButton).setOnClickListener {
            if (currentFilter == FilterType.FAVORITES) {
                // Si el filtro activo es FAVORITES, muestra todos los Pokémon
                (activity as MainActivity).applyFilter(FilterType.ALL)
                currentFilter = FilterType.ALL  // Restablece el filtro a "todos"
            } else {
                // Aplica el filtro FAVORITES
                (activity as MainActivity).applyFilter(FilterType.FAVORITES)
                currentFilter = FilterType.FAVORITES
            }
        }

        // Configura el botón de capturados
        view.findViewById<ImageButton>(R.id.capturedButton).setOnClickListener {
            if (currentFilter == FilterType.CAPTURED) {
                // Si el filtro activo es CAPTURED, muestra todos los Pokémon
                (activity as MainActivity).applyFilter(FilterType.ALL)
                currentFilter = FilterType.ALL  // Restablece el filtro a "todos"
            } else {
                // Aplica el filtro CAPTURED
                (activity as MainActivity).applyFilter(FilterType.CAPTURED)
                currentFilter = FilterType.CAPTURED
            }
        }

        return view
    }
}


package com.example.pokedex.ui.header

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.pokedex.R
import com.example.pokedex.data.model.FilterType
import com.example.pokedex.ui.main.MainActivity

class HeaderFragment : Fragment() {
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
            (activity as MainActivity).applyFilter(FilterType.FAVORITES)
        }

        // Configura el botón de capturados
        view.findViewById<ImageButton>(R.id.capturedButton).setOnClickListener {
            (activity as MainActivity).applyFilter(FilterType.CAPTURED)
        }

        return view
    }
}
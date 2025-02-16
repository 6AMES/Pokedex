package com.example.pokedex.ui.header

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R

class TypeAdapter(
    private val types: List<String>, // Lista de tipos de Pokémon
    private val onTypeSelected: (String) -> Unit // Callback para cuando se selecciona un tipo
) : RecyclerView.Adapter<TypeAdapter.TypeViewHolder>() {

    inner class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)

        fun bind(type: String) {
            val context = itemView.context

            // Mapeo de tipos a colores, igual que en el PokemonAdapter
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

            // Si el tipo es "all", mostramos "TODOS LOS TIPOS" con un color por defecto
            if (type == "all") {
                typeTextView.text = "TODOS LOS TIPOS"
                val defaultColor = ContextCompat.getColor(context, R.color.light_gray)
                val bgDrawable = GradientDrawable().apply {
                    setColor(defaultColor)
                    cornerRadius = 16f
                    setStroke(4, defaultColor)
                }
                typeTextView.background = bgDrawable
            } else {
                typeTextView.text = type.uppercase()
                // Buscamos el color asociado al tipo (convertimos a mayúsculas para que coincida con la clave)
                val colorResId = typeColors[type.uppercase()] ?: R.color.black
                val color = ContextCompat.getColor(context, colorResId)
                val bgDrawable = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = 16f
                    setStroke(4, color)
                }
                typeTextView.background = bgDrawable
            }

            // Asignamos el color de texto (por ejemplo, blanco)
            typeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))

            // Listener para cuando se selecciona un tipo
            itemView.setOnClickListener {
                onTypeSelected(type)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_type, parent, false)
        return TypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
        val type = types[position]
        holder.bind(type)
    }

    override fun getItemCount(): Int = types.size
}
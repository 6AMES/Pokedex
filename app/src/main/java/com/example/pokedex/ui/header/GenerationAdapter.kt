package com.example.pokedex.ui.header

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R

class GenerationAdapter(
    private val generations: List<String>, // Lista de generaciones
    private val onGenerationSelected: (String) -> Unit // Callback para cuando se selecciona una generación
) : RecyclerView.Adapter<GenerationAdapter.GenerationViewHolder>() {

    inner class GenerationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val generationTextView: TextView = itemView.findViewById(R.id.typeTextView)

        fun bind(generation: String) {
            val context = itemView.context

            // Si el tipo es "all", mostramos "TODAS LAS GENERACIONES" con un color por defecto
            if (generation == "all") {
                generationTextView.text = "TODAS LAS GENERACIONES"
                val defaultColor = ContextCompat.getColor(context, R.color.light_gray)
                val bgDrawable = GradientDrawable().apply {
                    setColor(defaultColor)
                    cornerRadius = 16f
                    setStroke(4, defaultColor)
                }
                generationTextView.background = bgDrawable
            } else {
                generationTextView.text = generation.uppercase()
                // Puedes asignar un color específico para cada generación si lo deseas
                val color = ContextCompat.getColor(context, R.color.light_gray) // Color por defecto
                val bgDrawable = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = 16f
                    setStroke(4, color)
                }
                generationTextView.background = bgDrawable
            }

            // Asignamos el color de texto (por ejemplo, blanco)
            generationTextView.setTextColor(ContextCompat.getColor(context, R.color.white))

            // Listener para cuando se selecciona una generación
            itemView.setOnClickListener {
                onGenerationSelected(generation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenerationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_type, parent, false)
        return GenerationViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenerationViewHolder, position: Int) {
        val generation = generations[position]
        holder.bind(generation)
    }

    override fun getItemCount(): Int = generations.size
}
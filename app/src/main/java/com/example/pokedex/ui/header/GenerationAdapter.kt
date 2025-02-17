package com.example.pokedex.ui.header

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R

class GenerationAdapter(
    private val generations: List<String>, // Lista de generaciones
    private val onGenerationSelected: (String) -> Unit // Callback para cuando se selecciona una generación
) : RecyclerView.Adapter<GenerationAdapter.GenerationViewHolder>() {

    inner class GenerationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val generationTextView: TextView = itemView.findViewById(R.id.typeTextView)

        fun bind(generation: String) {
            generationTextView.text = generation

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
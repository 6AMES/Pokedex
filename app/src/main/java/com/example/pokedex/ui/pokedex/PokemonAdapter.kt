package com.example.pokedex.ui.pokedex

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.data.model.Pokemon

class PokemonAdapter(
    private var pokemonList: List<Pokemon>,
    private val onFavoriteClick: (Pokemon) -> Unit,
    private val onCapturedClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val typesTextView: TextView = itemView.findViewById(R.id.typesTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val favoriteButton: Button = itemView.findViewById(R.id.favoriteButton)
        private val capturedButton: Button = itemView.findViewById(R.id.capturedButton)

        fun bind(pokemon: Pokemon) {
            nameTextView.text = pokemon.name
            idTextView.text = "#${pokemon.id}"
            typesTextView.text = pokemon.types.joinToString(", ")
            Glide.with(itemView.context).load(pokemon.imageUrl).into(imageView)

            // Configura los botones
            favoriteButton.text = if (pokemon.isFavorite) "Quitar favorito" else "Marcar favorito"
            capturedButton.text = if (pokemon.isCaptured) "Liberar" else "Capturar"

            favoriteButton.setOnClickListener {
                onFavoriteClick(pokemon)
            }

            capturedButton.setOnClickListener {
                onCapturedClick(pokemon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(pokemonList[position])
    }

    override fun getItemCount(): Int {
        return pokemonList.size
    }

    // Función para actualizar la lista de Pokémon
    fun updateList(newList: List<Pokemon>) {
        pokemonList = newList
        notifyDataSetChanged()
    }
}
package com.example.pokedex.ui.pokedex

import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.data.model.Pokemon
import java.util.Locale

class PokemonAdapter(
    private var pokemonList: MutableList<Pokemon>,
    private val onFavoriteClick: (Pokemon) -> Unit,
    private val onCapturedClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val typesTextView: TextView = itemView.findViewById(R.id.typesTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        private val capturedButton: ImageButton = itemView.findViewById(R.id.capturedButton)

        fun bind(pokemon: Pokemon) {
            Log.d("PokemonAdapter", "Pokémon: ${pokemon.name}, ID: ${pokemon.id}, Favorito: ${pokemon.isFavorite}, Capturado: ${pokemon.isCaptured}")
            nameTextView.text = pokemon.name.capitalize(Locale.ROOT)
            idTextView.text = "#${pokemon.id}"
            typesTextView.text = pokemon.types.joinToString(", ").uppercase()
            Glide.with(itemView.context).load(pokemon.imageUrl).into(imageView)

            favoriteButton.isSelected = pokemon.isFavorite
            capturedButton.isSelected = pokemon.isCaptured

            val favoriteIcon = if (pokemon.isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
            favoriteButton.setImageResource(favoriteIcon)

            val capturedIcon = if (pokemon.isCaptured) R.drawable.ic_captured_selected else R.drawable.ic_captured_unselected
            capturedButton.setImageResource(capturedIcon)

            favoriteButton.setOnClickListener { onFavoriteClick(pokemon) }
            capturedButton.setOnClickListener { onCapturedClick(pokemon) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(pokemonList[position])
    }

    override fun getItemCount(): Int = pokemonList.size

    fun updateList(newList: List<Pokemon>) {
        Log.d("PokemonAdapter", "Actualizando lista con ${newList.size} Pokémon")
        this.pokemonList = newList.toMutableList()
        notifyDataSetChanged()
    }
}
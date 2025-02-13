package com.example.pokedex.ui.pokedex

import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.data.model.Pokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PokemonAdapter(
    private val pokemonList: MutableList<Pokemon>,
    private val onFavoriteClick: (Pokemon) -> Unit,
    private val onCapturedClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val typesTextView: TextView = itemView.findViewById(R.id.typesTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        private val capturedButton: ImageButton = itemView.findViewById(R.id.capturedButton)

        fun bind(pokemon: Pokemon) {
            nameTextView.text = pokemon.name
            idTextView.text = "#${pokemon.id}"
            typesTextView.text = pokemon.types.joinToString(", ")
            Glide.with(itemView.context).load(pokemon.imageUrl).into(imageView)

            // Cambia el ícono del botón de favoritos según el estado
            val favoriteIcon = if (pokemon.isFavorite) {
                R.drawable.ic_favorite_selected
            } else {
                R.drawable.ic_favorite_unselected
            }
            favoriteButton.setImageResource(favoriteIcon)

            // Cambia el ícono del botón de capturados según el estado
            val capturedIcon = if (pokemon.isCaptured) {
                R.drawable.ic_captured_selected
            } else {
                R.drawable.ic_captured_unselected
            }
            capturedButton.setImageResource(capturedIcon)

            // Configura los clics en los botones
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
        Log.d("PokemonAdapter", "Actualizando lista con ${newList.size} Pokémon")
        pokemonList.clear()
        pokemonList.addAll(newList)
        notifyDataSetChanged()
    }
}
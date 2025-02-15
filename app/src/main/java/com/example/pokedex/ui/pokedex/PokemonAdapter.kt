package com.example.pokedex.ui.pokedex

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
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
        private val type1TextView: TextView = itemView.findViewById(R.id.type1TextView)
        private val type2TextView: TextView = itemView.findViewById(R.id.type2TextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        private val capturedButton: ImageButton = itemView.findViewById(R.id.capturedButton)

        fun bind(pokemon: Pokemon) {
            nameTextView.text = pokemon.name.capitalize(Locale.ROOT)
            idTextView.text = "#${pokemon.id}"

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

            val context = itemView.context

            // Aplicar color al primer tipo
            if (pokemon.types.isNotEmpty()) {
                type1TextView.text = pokemon.types[0].uppercase() // Texto del primer tipo
                val color1 = ContextCompat.getColor(context, typeColors[pokemon.types[0].uppercase()] ?: R.color.black)

                val bg1 = GradientDrawable()
                bg1.setColor(color1) // Color de fondo
                bg1.setCornerRadius(16f) // Bordes redondeados
                bg1.setStroke(4, color1) // Borde del mismo color

                type1TextView.background = bg1
                type1TextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Color del texto

                type1TextView.visibility = View.VISIBLE
            } else {
                type1TextView.visibility = View.GONE
            }

            // Aplicar color al segundo tipo si existe
            if (pokemon.types.size > 1) {
                type2TextView.text = pokemon.types[1].uppercase() // Texto del segundo tipo
                val color2 = ContextCompat.getColor(context, typeColors[pokemon.types[1].uppercase()] ?: R.color.black)

                val bg2 = GradientDrawable()
                bg2.setColor(color2)
                bg2.setCornerRadius(16f)
                bg2.setStroke(4, color2)

                type2TextView.background = bg2
                type2TextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Color del texto

                type2TextView.visibility = View.VISIBLE
            } else {
                type2TextView.visibility = View.GONE
            }

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
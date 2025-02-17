package com.example.pokedex.ui.header

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R

class GameAdapter(
    private val games: List<String>, // Lista de juegos
    private val onGameSelected: (String) -> Unit // Callback para cuando se selecciona un juego
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameTextView: TextView = itemView.findViewById(R.id.typeTextView)

        fun bind(game: String) {
            val context = itemView.context

            // Si el juego es "all", mostramos "TODOS LOS JUEGOS" con un color por defecto
            if (game == "all") {
                gameTextView.text = "TODOS LOS JUEGOS"
                val defaultColor = ContextCompat.getColor(context, R.color.light_gray)
                val bgDrawable = GradientDrawable().apply {
                    setColor(defaultColor)
                    cornerRadius = 16f
                    setStroke(4, defaultColor)
                }
                gameTextView.background = bgDrawable
            } else {
                gameTextView.text = game.uppercase()
                // Asignar color según el juego seleccionado
                val colorResId = when (game) {
                    "red" -> R.color.game_red
                    "blue" -> R.color.game_blue
                    "yellow" -> R.color.game_yellow
                    "gold" -> R.color.game_gold
                    "silver" -> R.color.game_silver
                    "crystal" -> R.color.game_crystal
                    "ruby" -> R.color.game_ruby
                    "sapphire" -> R.color.game_sapphire
                    "emerald" -> R.color.game_emerald
                    "firered" -> R.color.game_firered
                    "leafgreen" -> R.color.game_leafgreen
                    "diamond" -> R.color.game_diamond
                    "pearl" -> R.color.game_pearl
                    "platinum" -> R.color.game_platinum
                    "heartgold" -> R.color.game_heartgold
                    "soulsilver" -> R.color.game_soulsilver
                    "black" -> R.color.game_black
                    "white" -> R.color.game_white
                    "x" -> R.color.game_x
                    "y" -> R.color.game_y
                    "omega_ruby" -> R.color.game_omega_ruby
                    "alpha_sapphire" -> R.color.game_alpha_sapphire
                    "sun" -> R.color.game_sun
                    "moon" -> R.color.game_moon
                    "ultra_sun" -> R.color.game_ultra_sun
                    "ultra_moon" -> R.color.game_ultra_moon
                    "sword" -> R.color.game_sword
                    "shield" -> R.color.game_shield
                    "scarlet" -> R.color.game_scarlet
                    "violet" -> R.color.game_violet
                    else -> R.color.light_gray
                }
                val color = ContextCompat.getColor(context, colorResId)
                val bgDrawable = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = 16f
                    setStroke(4, color)
                }
                gameTextView.background = bgDrawable
            }

            // Asignamos el color de texto (por ejemplo, blanco)
            gameTextView.setTextColor(ContextCompat.getColor(context, R.color.white))

            // Listener para cuando se selecciona un juego
            itemView.setOnClickListener {
                onGameSelected(game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_type, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.bind(game)
    }

    override fun getItemCount(): Int = games.size
}
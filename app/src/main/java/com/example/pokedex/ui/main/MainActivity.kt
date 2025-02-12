package com.example.pokedex.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pokedex.R
import com.example.pokedex.model.FilterType
import com.example.pokedex.ui.header.HeaderFragment
import com.example.pokedex.ui.pokedex.PokedexFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializa las vistas
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Configura el menú lateral
        setupNavigationDrawer()

        // Abre el fragmento de la Pokédex por defecto
        if (savedInstanceState == null) {
            replaceFragment(PokedexFragment())
            navigationView.setCheckedItem(R.id.nav_pokedex)
        }
    }

    private fun setupNavigationDrawer() {
        // Configura el listener para los elementos del menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_pokedex -> {
                    replaceFragment(PokedexFragment())
                }
            }
            // Cierra el menú lateral después de seleccionar una opción
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Función para reemplazar fragmentos en el contenedor principal
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Función para abrir el menú lateral
    fun openDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Función para aplicar filtros en el PokedexFragment
    fun applyFilter(filterType: FilterType) {
        val pokedexFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? PokedexFragment
        pokedexFragment?.applyFilter(filterType)
    }
}
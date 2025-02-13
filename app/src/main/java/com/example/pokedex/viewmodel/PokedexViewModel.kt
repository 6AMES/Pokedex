package com.example.pokedex.viewmodel

import androidx.lifecycle.*
import com.example.pokedex.data.model.FilterType

class PokedexViewModel : ViewModel() {

    // LiveData para observar el filtro actual
    private val _filterType = MutableLiveData<FilterType>(FilterType.ALL)
    val filterType: LiveData<FilterType> get() = _filterType

    // Función para actualizar el filtro
    fun setFilter(filter: FilterType) {
        _filterType.value = filter
    }
}

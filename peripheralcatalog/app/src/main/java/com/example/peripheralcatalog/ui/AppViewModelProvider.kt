package com.example.peripheralcatalog.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.peripheralcatalog.PeripheralCatalogApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as PeripheralCatalogApp)
            CatalogViewModel(application.container.repository)
        }
    }
}


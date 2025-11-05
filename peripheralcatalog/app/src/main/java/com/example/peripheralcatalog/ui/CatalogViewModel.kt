package com.example.peripheralcatalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peripheralcatalog.data.repository.PeripheralsRepository
import com.example.peripheralcatalog.domain.model.Peripheral
import com.example.peripheralcatalog.domain.model.PeripheralHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FilterState(
    val searchTerm: String = "",
    val selectedCategory: String? = null,
    val selectedBrand: String? = null,
    val minPrice: Float = 0f,
    val maxPrice: Float = 0f,
    val currentPriceRange: ClosedFloatingPointRange<Float> = 0f..0f,
    val onlyWireless: Boolean = false,
    val onlyRgb: Boolean = false,
    val onlyMechanical: Boolean = false
)

data class CatalogUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val allPeripherals: List<Peripheral> = emptyList(),
    val filteredPeripherals: List<Peripheral> = emptyList(),
    val favorites: List<Peripheral> = emptyList(),
    val history: List<PeripheralHistoryItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val comparisonSelection: Set<String> = emptySet(),
    val filterState: FilterState = FilterState(),
    val lastUpdated: Long? = null,
    val errorMessage: String? = null
)

class CatalogViewModel(
    private val repository: PeripheralsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState(isLoading = true))
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        observePeripherals()
        observeFavorites()
        observeHistory()
        refresh()
        loadCategories()
    }

    private fun observePeripherals() {
        viewModelScope.launch {
            repository.peripheralsFlow.collect { peripherals ->
                val categories = peripherals.map { it.category }.distinct().sorted()
                val brands = peripherals.map { it.brand }.distinct().sorted()
                val minPrice = peripherals.minOfOrNull { it.price }?.toFloat() ?: 0f
                val maxPrice = peripherals.maxOfOrNull { it.price }?.toFloat() ?: 0f
                _state.updateState {
                    val updatedFilter = it.filterState.adjustPriceRange(minPrice, maxPrice)
                    val filtered = applyFilters(peripherals, updatedFilter)
                    it.copy(
                        isLoading = false,
                        allPeripherals = peripherals,
                        filteredPeripherals = filtered,
                        categories = categories,
                        brands = brands,
                        filterState = updatedFilter
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.favoritesFlow.collect { favorites ->
                _state.updateState {
                    it.copy(favorites = favorites, comparisonSelection = it.comparisonSelection.filterToSet(favorites))
                }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.historyFlow.collect { history ->
                _state.updateState { it.copy(history = history) }
            }
        }
    }

    fun refresh(category: String? = null) {
        viewModelScope.launch {
            _state.updateState { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching { repository.refresh(category) }
                .onSuccess {
                    _state.updateState { it.copy(isRefreshing = false, lastUpdated = System.currentTimeMillis()) }
                }
                .onFailure { throwable ->
                    _state.updateState {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Falha ao sincronizar dados"
                        )
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val remoteCategories = repository.fetchCategories()
            if (remoteCategories.isNotEmpty()) {
                _state.updateState { it.copy(categories = remoteCategories.sorted()) }
            }
        }
    }

    fun selectCategory(category: String?) {
        _state.updateState { current ->
            val filter = current.filterState.copy(selectedCategory = category)
            current.copy(
                filterState = filter,
                filteredPeripherals = applyFilters(current.allPeripherals, filter)
            )
        }
    }

    fun setSearchTerm(term: String) {
        _state.updateState { current ->
            val filter = current.filterState.copy(searchTerm = term)
            current.copy(
                filterState = filter,
                filteredPeripherals = applyFilters(current.allPeripherals, filter)
            )
        }
    }

    fun selectBrand(brand: String?) {
        _state.updateState { current ->
            val filter = current.filterState.copy(selectedBrand = brand)
            current.copy(
                filterState = filter,
                filteredPeripherals = applyFilters(current.allPeripherals, filter)
            )
        }
    }

    fun updatePriceRange(range: ClosedFloatingPointRange<Float>) {
        _state.updateState { current ->
            val filter = current.filterState.copy(currentPriceRange = range)
            current.copy(
                filterState = filter,
                filteredPeripherals = applyFilters(current.allPeripherals, filter)
            )
        }
    }

    fun toggleWireless() {
        toggleFeature { current -> current.copy(onlyWireless = !current.onlyWireless) }
    }

    fun toggleRgb() {
        toggleFeature { current -> current.copy(onlyRgb = !current.onlyRgb) }
    }

    fun toggleMechanical() {
        toggleFeature { current -> current.copy(onlyMechanical = !current.onlyMechanical) }
    }

    private fun toggleFeature(transform: (FilterState) -> FilterState) {
        _state.updateState { current ->
            val filter = transform(current.filterState)
            current.copy(
                filterState = filter,
                filteredPeripherals = applyFilters(current.allPeripherals, filter)
            )
        }
    }

    fun clearFilters() {
        _state.updateState { current ->
            val cleared = current.filterState.copy(
                searchTerm = "",
                selectedCategory = null,
                selectedBrand = null,
                currentPriceRange = current.filterState.minPrice..current.filterState.maxPrice,
                onlyWireless = false,
                onlyRgb = false,
                onlyMechanical = false
            )
            current.copy(
                filterState = cleared,
                filteredPeripherals = applyFilters(current.allPeripherals, cleared)
            )
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun toggleComparison(id: String) {
        _state.updateState { current ->
            val selection = current.comparisonSelection.toMutableSet()
            if (selection.contains(id)) {
                selection.remove(id)
            } else if (selection.size < 3) {
                selection.add(id)
            }
            current.copy(comparisonSelection = selection)
        }
    }

    fun clearComparison() {
        _state.updateState { it.copy(comparisonSelection = emptySet()) }
    }

    fun removeFromComparison(id: String) {
        _state.updateState { current ->
            val updated = current.comparisonSelection.toMutableSet()
            updated.remove(id)
            current.copy(comparisonSelection = updated)
        }
    }

    fun recordView(id: String) {
        viewModelScope.launch {
            repository.recordView(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    suspend fun getPeripheralsForComparison(): List<Peripheral> {
        val ids = state.value.comparisonSelection
        return repository.getPeripheralsByIds(ids)
    }

    fun observePeripheral(id: String): Flow<Peripheral?> = repository.peripheralFlow(id)

    private fun applyFilters(
        peripherals: List<Peripheral>,
        filters: FilterState
    ): List<Peripheral> {
        return peripherals.asSequence()
            .filter { filters.selectedCategory == null || it.category.equals(filters.selectedCategory, ignoreCase = true) }
            .filter { filters.selectedBrand == null || it.brand.equals(filters.selectedBrand, ignoreCase = true) }
            .filter {
                if (filters.searchTerm.isBlank()) true else {
                    val query = filters.searchTerm.lowercase()
                    it.name.lowercase().contains(query) ||
                        it.brand.lowercase().contains(query) ||
                        it.description.lowercase().contains(query)
                }
            }
            .filter {
                if (filters.currentPriceRange.endInclusive == 0f && filters.currentPriceRange.start == 0f) {
                    true
                } else {
                    val price = it.price.toFloat()
                    price >= filters.currentPriceRange.start && price <= filters.currentPriceRange.endInclusive
                }
            }
            .filter {
                if (!filters.onlyWireless) true else it.features.any { feature -> feature.equals("Wireless", ignoreCase = true) || feature.equals("Bluetooth", ignoreCase = true) }
            }
            .filter {
                if (!filters.onlyRgb) true else it.features.any { feature -> feature.equals("RGB", ignoreCase = true) }
            }
            .filter {
                if (!filters.onlyMechanical) true else it.features.any { feature -> feature.equals("Mecanico", ignoreCase = true) }
            }
            .toList()
            .sortedBy { it.name }
    }

    private fun FilterState.adjustPriceRange(min: Float, max: Float): FilterState {
        if (min == 0f && max == 0f) return copy(minPrice = min, maxPrice = max, currentPriceRange = min..max)
        val clampedRange = if (currentPriceRange.start == 0f && currentPriceRange.endInclusive == 0f) {
            min..max
        } else {
            val start = currentPriceRange.start.coerceIn(min, max)
            val end = currentPriceRange.endInclusive.coerceIn(min, max)
            val correctedEnd = if (end < start) start else end
            start..correctedEnd
        }
        return copy(minPrice = min, maxPrice = max, currentPriceRange = clampedRange)
    }

    private fun Set<String>.filterToSet(peripherals: List<Peripheral>): Set<String> {
        val ids = peripherals.map { it.id }.toSet()
        return filter { ids.contains(it) }.toSet()
    }

    private inline fun MutableStateFlow<CatalogUiState>.updateState(
        crossinline transform: (CatalogUiState) -> CatalogUiState
    ) {
        value = transform(value)
    }
}


package com.example.peripheralcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.peripheralcatalog.domain.model.Peripheral
import com.example.peripheralcatalog.ui.CatalogUiState
import com.example.peripheralcatalog.ui.components.PeripheralGrid
import kotlin.ranges.ClosedFloatingPointRange
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: CatalogUiState,
    onSearch: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onUpdatePriceRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onToggleWireless: () -> Unit,
    onToggleRgb: () -> Unit,
    onToggleMechanical: () -> Unit,
    onClearFilters: () -> Unit,
    onPeripheralClick: (Peripheral) -> Unit,
    onToggleFavorite: (Peripheral) -> Unit,
    onToggleComparison: (Peripheral) -> Unit,
    onBack: () -> Unit
) {
    var queryState by remember { mutableStateOf(TextFieldValue(state.filterState.searchTerm)) }
    val priceRange = state.filterState.currentPriceRange

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Busca e Filtros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = queryState,
                onValueChange = {
                    queryState = it
                    onSearch(it.text)
                },
                label = { Text(text = "Buscar por nome ou marca") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Categorias", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterState.selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text(text = "Todas") }
                    )
                }
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.filterState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(text = category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Marcas", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterState.selectedBrand == null,
                        onClick = { onBrandSelected(null) },
                        label = { Text(text = "Todas") }
                    )
                }
                items(state.brands) { brand ->
                    FilterChip(
                        selected = state.filterState.selectedBrand == brand,
                        onClick = { onBrandSelected(brand) },
                        label = { Text(text = brand) }
                    )
                }
            }

            Text(text = "Faixa de preco", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "R$ %.0f - R$ %.0f".format(priceRange.start, priceRange.endInclusive),
                style = MaterialTheme.typography.bodyMedium
            )
            if (state.filterState.maxPrice > state.filterState.minPrice) {
                RangeSlider(
                    value = priceRange,
                    onValueChange = onUpdatePriceRange,
                    valueRange = state.filterState.minPrice..state.filterState.maxPrice,
                    steps = 10)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Caracteristicas", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.filterState.onlyWireless,
                    onClick = onToggleWireless,
                    label = { Text(text = "Wireless") }
                )
                FilterChip(
                    selected = state.filterState.onlyRgb,
                    onClick = onToggleRgb,
                    label = { Text(text = "RGB") }
                )
                FilterChip(
                    selected = state.filterState.onlyMechanical,
                    onClick = onToggleMechanical,
                    label = { Text(text = "Mecanico") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearFilters) {
                Text(text = "Limpar filtros")
            }

            Spacer(modifier = Modifier.height(16.dp))

            PeripheralGrid(
                peripherals = state.filteredPeripherals,
                comparisonSelection = state.comparisonSelection,
                onClick = onPeripheralClick,
                onToggleFavorite = onToggleFavorite,
                onToggleComparison = onToggleComparison
            )
        }
    }
}


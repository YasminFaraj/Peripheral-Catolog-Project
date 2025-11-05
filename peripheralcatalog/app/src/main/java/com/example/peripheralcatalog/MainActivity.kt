package com.example.peripheralcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.peripheralcatalog.ui.AppViewModelProvider
import com.example.peripheralcatalog.ui.CatalogViewModel
import com.example.peripheralcatalog.ui.navigation.CatalogDestinations
import com.example.peripheralcatalog.ui.screens.ComparisonScreen
import com.example.peripheralcatalog.ui.screens.DetailScreen
import com.example.peripheralcatalog.ui.screens.FavoritesScreen
import com.example.peripheralcatalog.ui.screens.HistoryScreen
import com.example.peripheralcatalog.ui.screens.HomeScreen
import com.example.peripheralcatalog.ui.screens.SearchScreen
import com.example.peripheralcatalog.ui.screens.SplashScreen
import com.example.peripheralcatalog.ui.theme.PeripheralCatalogTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CatalogViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            PeripheralCatalogTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = CatalogDestinations.Splash
                ) {
                    composable(CatalogDestinations.Splash) {
                        SplashScreen(
                            isLoading = state.isLoading,
                            onFinished = {
                                navController.navigate(CatalogDestinations.Home) {
                                    popUpTo(CatalogDestinations.Splash) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(CatalogDestinations.Home) {
                        HomeScreen(
                            state = state,
                            onPeripheralClick = { peripheral ->
                                navController.navigate("${CatalogDestinations.Details}/${peripheral.id}")
                            },
                            onCategorySelected = { category ->
                                viewModel.selectCategory(category)
                            },
                            onToggleFavorite = { peripheral ->
                                viewModel.toggleFavorite(peripheral.id)
                            },
                            onToggleComparison = { peripheral ->
                                viewModel.toggleComparison(peripheral.id)
                            },
                            onRefresh = { viewModel.refresh() },
                            onNavigateToSearch = { navController.navigate(CatalogDestinations.Search) },
                            onNavigateToFavorites = { navController.navigate(CatalogDestinations.Favorites) },
                            onNavigateToHistory = { navController.navigate(CatalogDestinations.History) },
                            onNavigateToComparison = { navController.navigate(CatalogDestinations.Comparison) }
                        )
                    }

                    composable(
                        route = CatalogDestinations.DetailsRoute,
                        arguments = listOf(navArgument(CatalogDestinations.DetailsArgId) { type = NavType.StringType })
                    ) { backStackEntry ->
                        val peripheralId = backStackEntry.arguments?.getString(CatalogDestinations.DetailsArgId)
                        if (peripheralId == null) {
                            navController.navigateUp()
                            return@composable
                        }
                        val peripheral by viewModel.observePeripheral(peripheralId).collectAsStateWithLifecycle(initialValue = null)
                        DetailScreen(
                            peripheral = peripheral,
                            isSelectedForComparison = state.comparisonSelection.contains(peripheralId),
                            onToggleFavorite = {
                                viewModel.toggleFavorite(peripheralId)
                            },
                            onToggleComparison = {
                                viewModel.toggleComparison(peripheralId)
                            },
                            onBack = { navController.navigateUp() },
                            onViewed = {
                                viewModel.recordView(peripheralId)
                            }
                        )
                    }

                    composable(CatalogDestinations.Favorites) {
                        FavoritesScreen(
                            favorites = state.favorites,
                            comparisonSelection = state.comparisonSelection,
                            onPeripheralClick = { peripheral ->
                                navController.navigate("${CatalogDestinations.Details}/${peripheral.id}")
                            },
                            onToggleFavorite = { peripheral ->
                                viewModel.toggleFavorite(peripheral.id)
                            },
                            onToggleComparison = { peripheral ->
                                viewModel.toggleComparison(peripheral.id)
                            },
                            onBack = { navController.navigateUp() }
                        )
                    }

                    composable(CatalogDestinations.Search) {
                        SearchScreen(
                            state = state,
                            onSearch = { query -> viewModel.setSearchTerm(query) },
                            onCategorySelected = { category -> viewModel.selectCategory(category) },
                            onBrandSelected = { brand -> viewModel.selectBrand(brand) },
                            onUpdatePriceRange = { range -> viewModel.updatePriceRange(range) },
                            onToggleWireless = { viewModel.toggleWireless() },
                            onToggleRgb = { viewModel.toggleRgb() },
                            onToggleMechanical = { viewModel.toggleMechanical() },
                            onClearFilters = { viewModel.clearFilters() },
                            onPeripheralClick = { peripheral ->
                                navController.navigate("${CatalogDestinations.Details}/${peripheral.id}")
                            },
                            onToggleFavorite = { peripheral ->
                                viewModel.toggleFavorite(peripheral.id)
                            },
                            onToggleComparison = { peripheral ->
                                viewModel.toggleComparison(peripheral.id)
                            },
                            onBack = { navController.navigateUp() }
                        )
                    }

                    composable(CatalogDestinations.Comparison) {
                        ComparisonScreen(
                            peripherals = state.allPeripherals.filter { state.comparisonSelection.contains(it.id) },
                            onRemove = { id -> viewModel.removeFromComparison(id) },
                            onClear = { viewModel.clearComparison() },
                            onBack = { navController.navigateUp() }
                        )
                    }

                    composable(CatalogDestinations.History) {
                        HistoryScreen(
                            historyItems = state.history,
                            onPeripheralClick = { item ->
                                navController.navigate("${CatalogDestinations.Details}/${item.peripheral.id}")
                            },
                            onClear = { viewModel.clearHistory() },
                            onBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
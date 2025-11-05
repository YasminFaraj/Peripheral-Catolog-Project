package com.example.peripheralcatalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.peripheralcatalog.domain.model.Peripheral

@Composable
fun PeripheralGrid(
    peripherals: List<Peripheral>,
    comparisonSelection: Set<String>,
    onClick: (Peripheral) -> Unit,
    onToggleFavorite: (Peripheral) -> Unit,
    onToggleComparison: (Peripheral) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier
    ) {
        items(peripherals, key = { it.id }) { peripheral ->
            PeripheralCard(
                peripheral = peripheral,
                isSelectedForComparison = comparisonSelection.contains(peripheral.id),
                onClick = { onClick(peripheral) },
                onToggleFavorite = { onToggleFavorite(peripheral) },
                onToggleComparison = { onToggleComparison(peripheral) }
            )
        }
    }
}

@Composable
fun PeripheralCard(
    peripheral: Peripheral,
    isSelectedForComparison: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.clickable { onClick() }) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(peripheral.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = peripheral.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (peripheral.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (peripheral.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Favoritar"
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = peripheral.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = peripheral.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "R$ %.2f".format(peripheral.price),
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 18.sp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AssistChip(
                        onClick = onToggleComparison,
                        label = { Text(text = if (isSelectedForComparison) "Selecionado" else "Comparar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelectedForComparison) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (isSelectedForComparison) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FeatureRow(peripheral = peripheral)
            }
        }
    }
}

@Composable
private fun FeatureRow(peripheral: Peripheral) {
    val features = peripheral.features.take(3)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        features.forEach { feature ->
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


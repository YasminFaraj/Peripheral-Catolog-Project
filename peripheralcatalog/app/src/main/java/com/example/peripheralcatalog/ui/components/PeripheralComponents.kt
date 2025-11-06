package com.example.peripheralcatalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.peripheralcatalog.domain.model.Peripheral

private val DarkBlue = Color(0xFF0A1E3F)
private val LightBlue = Color(0xFF1976D2)

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
        columns = GridCells.Adaptive(minSize = 220.dp), // Aumentado de 180 -> 220
        modifier = modifier
            .background(Color.White)
            .padding(12.dp)
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
            .padding(10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
        ) {
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
                        .height(170.dp) // imagem maior
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (peripheral.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (peripheral.isFavorite) Color.Red else DarkBlue,
                        contentDescription = "Favoritar"
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = peripheral.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DarkBlue,
                        fontSize = 18.sp // aumentado
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = peripheral.brand,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "R$ %.2f".format(peripheral.price),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 20.sp, // maior destaque
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onToggleComparison,
                        modifier = Modifier
                            .height(46.dp)
                            .width(140.dp), // botão maior
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelectedForComparison)
                                LightBlue
                            else
                                DarkBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = if (isSelectedForComparison) "Selecionado" else "Comparar",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                FeatureRow(peripheral = peripheral)
            }
        }
    }
}

@Composable
private fun FeatureRow(peripheral: Peripheral) {
    val features = peripheral.features.take(3)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        features.forEach { feature ->
            Box(
                modifier = Modifier
                    .background(
                        color = DarkBlue,
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp) // etiquetas maiores
            ) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

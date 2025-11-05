package com.example.peripheralcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.peripheralcatalog.domain.model.Peripheral
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    peripheral: Peripheral?,
    isSelectedForComparison: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleComparison: () -> Unit,
    onBack: () -> Unit,
    onViewed: (() -> Unit)? = null
) {
    val context = LocalContext.current

    LaunchedEffect(peripheral?.id) {
        if (peripheral != null) {
            onViewed?.invoke()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = peripheral?.name ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite, enabled = peripheral != null) {
                        Icon(
                            imageVector = if (peripheral?.isFavorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritar"
                        )
                    }
                    AssistChip(
                        onClick = onToggleComparison,
                        enabled = peripheral != null,
                        label = { Text(text = if (isSelectedForComparison) "Comparando" else "Comparar") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.SwapHoriz, contentDescription = null)
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelectedForComparison) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        if (peripheral == null) {
            Text(
                text = "Carregando dados...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(peripheral.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = peripheral.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = peripheral.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = peripheral.brand,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categoria: ${peripheral.category}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Preco sugerido: R$ %.2f".format(peripheral.price),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Descricao", style = MaterialTheme.typography.titleMedium)
                Text(text = peripheral.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Especificacoes", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    peripheral.specs.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = key, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = value, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (peripheral.features.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Destaques", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        peripheral.features.forEach { feature ->
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(text = feature) }
                            )
                        }
                    }
                }
            }
        }
    }
}


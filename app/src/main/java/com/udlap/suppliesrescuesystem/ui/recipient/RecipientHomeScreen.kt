package com.udlap.suppliesrescuesystem.ui.recipient

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.ui.components.AppDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientHomeScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: RecipientViewModel = hiltViewModel()
) {
    val incomingBatches by viewModel.incomingBatches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    AppDrawer(
        currentRoute = "home",
        onNavigateToHome = { },
        onNavigateToProfile = onNavigateToProfile
    ) { drawerState, scope ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ENTREGAS ENTRANTES", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (incomingBatches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay entregas pendientes.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(incomingBatches) { batch ->
                            IncomingBatchItem(
                                batch = batch,
                                onConfirm = { viewModel.confirmReception(batch.id) },
                                onDelete = { viewModel.deleteBatch(batch.id) },
                                isLoading = uiState is RecipientState.Loading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomingBatchItem(
    batch: RescueBatch,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val expirationDate = remember(batch.expiresAt) { sdf.format(Date(batch.expiresAt)) }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, enabled = !isLoading) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
            Text(text = "De: ${batch.donorName}", fontSize = 14.sp)
            Text(text = "Expira: $expirationDate", fontSize = 12.sp, color = Color.Gray)
            
            if (batch.status == "DELIVERED") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isLoading
                ) {
                    Text("CONFIRMAR RECEPCIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

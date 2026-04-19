package com.udlap.suppliesrescuesystem.ui.volunteer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.ui.components.AppDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: VolunteerViewModel = hiltViewModel()
) {
    val availableBatches by viewModel.availableBatches.collectAsState()
    val activeRescue by viewModel.activeRescue.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    AppDrawer(
        currentRoute = "home",
        onNavigateToHome = { },
        onNavigateToProfile = onNavigateToProfile
    ) { drawerState, scope ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RESCATES DISPONIBLES", fontWeight = FontWeight.ExtraBold) },
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
                if (activeRescue != null) {
                    ActiveRescueCard(
                        batch = activeRescue!!,
                        onComplete = { viewModel.completeRescue(activeRescue!!.id) },
                        onOpenMap = { address -> openMap(context, address) },
                        isLoading = uiState is VolunteerState.Loading
                    )
                } else {
                    if (availableBatches.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay rescates disponibles por ahora.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(availableBatches) { batch ->
                                AvailableRescueItem(
                                    batch = batch,
                                    onClaim = { viewModel.claimRescue(batch.id) },
                                    isLoading = uiState is VolunteerState.Loading
                                )
                            }
                        }
                    }
                }
                
                if (uiState is VolunteerState.Error) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetState() },
                        title = { Text("Aviso") },
                        text = { Text((uiState as VolunteerState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.resetState() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveRescueCard(
    batch: RescueBatch,
    onComplete: () -> Unit,
    onOpenMap: (String) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ESTÁS EN RUTA", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF1976D2))
            Text(batch.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ORIGEN:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(batch.donorAddress, fontSize = 14.sp)
            Button(onClick = { onOpenMap(batch.donorAddress) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("VER MAPA")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("COMPLETAR ENTREGA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AvailableRescueItem(
    batch: RescueBatch,
    onClaim: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Origen: ${batch.donorName}", fontSize = 14.sp)
            Text(text = "Destino: ${batch.recipientName}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClaim,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("RECLAMAR RESCATE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun openMap(context: Context, address: String) {
    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
    }
}

package com.udlap.suppliesrescuesystem.ui.volunteer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun VolunteerHomeScreen(
    viewModel: VolunteerViewModel = hiltViewModel()
) {
    val availableBatches by viewModel.availableBatches.collectAsState()
    val activeRescue by viewModel.activeRescue.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
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
                Text(
                    text = "AVAILABLE RESCUES",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (availableBatches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No rescues available right now.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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

@Composable
fun ActiveRescueCard(
    batch: RescueBatch,
    onComplete: () -> Unit,
    onOpenMap: (String) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ESTÁS EN RUTA",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color(0xFF1976D2)
            )
            Text(
                text = batch.title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("PUNTO DE RECOGIDA:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(batch.donorName, fontSize = 16.sp)
            Text(batch.donorAddress, fontSize = 14.sp, color = Color.DarkGray)
            Button(
                onClick = { onOpenMap(batch.donorAddress) },
                modifier = Modifier.padding(top = 4.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("IR A RECOGIDA")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("PUNTO DE ENTREGA:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(batch.recipientName, fontSize = 16.sp)
            Text(batch.recipientAddress, fontSize = 14.sp, color = Color.DarkGray)
            Button(
                onClick = { onOpenMap(batch.recipientAddress) },
                modifier = Modifier.padding(top = 4.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("IR A ENTREGA")
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    Text("MARCAR COMO ENTREGADO", fontWeight = FontWeight.Bold)
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "De: ${batch.donorName}", fontSize = 14.sp)
            Text(text = "Hacia: ${batch.recipientName}", fontSize = 14.sp)
            Text(text = "Ventana: ${batch.pickupWindow}", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onClaim,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("RECLAMAR ESTE RESCATE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Opens an external mapping application (e.g., Google Maps) to navigate to the specified address.
 *
 * @param context The current [Context].
 * @param address The destination address as a string.
 */
private fun openMap(context: Context, address: String) {
    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    // Try to open with Google Maps specifically if available, else system default
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
    }
}

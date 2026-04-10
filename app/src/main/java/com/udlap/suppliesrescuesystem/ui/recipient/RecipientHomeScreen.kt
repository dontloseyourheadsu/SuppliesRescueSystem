package com.udlap.suppliesrescuesystem.ui.recipient

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch

@Composable
fun RecipientHomeScreen(
    viewModel: RecipientViewModel = hiltViewModel()
) {
    val incomingBatches by viewModel.incomingBatches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "ENTREGAS PARA NOSOTROS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (incomingBatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay entregas pendientes.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(incomingBatches) { batch ->
                        IncomingBatchItem(
                            batch = batch,
                            onConfirm = { viewModel.confirmReception(batch.id) },
                            isLoading = uiState is RecipientState.Loading
                        )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Surface(
                    color = if (batch.status == "DELIVERED") Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (batch.status == "DELIVERED") "LLEGÓ" else "EN CAMINO",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (batch.status == "DELIVERED") Color(0xFF2E7D32) else Color(0xFF1976D2)
                    )
                }
            }
            
            Text(text = "Donante: ${batch.donorName}", fontSize = 14.sp)
            Text(text = "Cantidad: ${batch.quantity}", fontSize = 14.sp, color = Color.Gray)
            
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

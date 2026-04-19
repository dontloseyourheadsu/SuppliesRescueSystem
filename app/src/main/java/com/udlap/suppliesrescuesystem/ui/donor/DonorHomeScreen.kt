package com.udlap.suppliesrescuesystem.ui.donor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHomeScreen(
    onNavigateToPublish: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: RescueViewModel = hiltViewModel()
) {
    val myBatches by viewModel.myBatches.collectAsState()
    
    AppDrawer(
        currentRoute = "home",
        onNavigateToHome = { },
        onNavigateToProfile = onNavigateToProfile
    ) { drawerState, scope ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MIS RESCATES", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToPublish,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Publish Batch")
                }
            },
            containerColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (myBatches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay lotes publicados aún.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(myBatches) { batch -> BatchItem(batch) }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchItem(batch: RescueBatch) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(text = batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Qty: ${batch.quantity}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Window: ${batch.pickupWindow}", fontSize = 14.sp, color = Color.DarkGray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val statusLabel = when (batch.status) {
                "AVAILABLE" -> "Pendiente"
                "CLAIMED" -> "Asignado"
                "DELIVERED" -> "Completado"
                else -> batch.status
            }

            Surface(
                color = if(batch.status == "AVAILABLE") Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
            ) {
                Text(
                    text = statusLabel.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

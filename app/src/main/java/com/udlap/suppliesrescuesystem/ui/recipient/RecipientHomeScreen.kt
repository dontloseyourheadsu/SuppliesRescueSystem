package com.udlap.suppliesrescuesystem.ui.recipient

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.model.RecipientNeed
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
    val myNeeds by viewModel.myNeeds.collectAsState()
    val openBatches by viewModel.openBatches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddNeedDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is RecipientState.Error) {
            snackbarHostState.showSnackbar((uiState as RecipientState.Error).message)
            viewModel.resetState()
        } else if (uiState is RecipientState.Success) {
            viewModel.resetState()
        }
    }

    AppDrawer(
        currentRoute = "home",
        onNavigateToHome = { },
        onNavigateToProfile = onNavigateToProfile
    ) { drawerState, scope ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("RECIPIENT PORTAL", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                if (selectedTab == 1) {
                    val canAddNeed = myNeeds.size < 3
                    Column(horizontalAlignment = Alignment.End) {
                        if (!canAddNeed) {
                            Card(
                                modifier = Modifier.padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Text(
                                    "Máximo 3 necesidades activas.",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { if (canAddNeed) showAddNeedDialog = true },
                            containerColor = if (canAddNeed) Color(0xFF4CAF50) else Color.LightGray,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Need")
                        }
                    }
                }
            },
            containerColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF4CAF50)
                        )
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("INCOMING", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("MY NEEDS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("BROWSE OPEN", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (selectedTab) {
                        0 -> IncomingList(incomingBatches, viewModel, uiState)
                        1 -> NeedsList(myNeeds, viewModel, uiState)
                        2 -> OpenBatchesList(openBatches, viewModel, uiState)
                    }
                }
            }
        }
    }

    if (showAddNeedDialog) {
        AddNeedDialog(
            onDismiss = { showAddNeedDialog = false },
            onConfirm = { desc ->
                viewModel.publishNeed(desc)
                showAddNeedDialog = false
            }
        )
    }
}

@Composable
fun IncomingList(batches: List<RescueBatch>, viewModel: RecipientViewModel, uiState: RecipientState) {
    if (batches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay entregas pendientes.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(batches) { batch ->
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

@Composable
fun NeedsList(needs: List<RecipientNeed>, viewModel: RecipientViewModel, uiState: RecipientState) {
    if (needs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No has publicado necesidades.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(needs) { need ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(need.description, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        IconButton(onClick = { viewModel.deleteNeed(need.id) }, enabled = uiState !is RecipientState.Loading) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenBatchesList(batches: List<RescueBatch>, viewModel: RecipientViewModel, uiState: RecipientState) {
    if (batches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay lotes abiertos disponibles.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(batches) { batch ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(batch.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Donor: ${batch.donorName}", fontSize = 14.sp)
                        Text("Quantity: ${batch.quantity}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.claimOpenBatch(batch.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = uiState !is RecipientState.Loading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("REQUEST THIS BATCH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNeedDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var description by remember { mutableStateOf("") }
    val maxChars = 60
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué necesitas?") },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { if (it.length <= maxChars) description = it },
                    placeholder = { Text("ej. 5kg de arroz, pan, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${description.length} / $maxChars",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            fontSize = 10.sp
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (description.isNotBlank()) onConfirm(description) }) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
            } else if (batch.status == "AVAILABLE") {
                Text("Status: Buscando voluntario", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            } else if (batch.status == "CLAIMED") {
                Text("Status: Voluntario asignado", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            } else if (batch.status == "COLLECTED") {
                Text("Status: En camino (Recolectado)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

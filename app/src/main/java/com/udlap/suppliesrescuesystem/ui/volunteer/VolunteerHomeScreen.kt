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

import com.udlap.suppliesrescuesystem.util.TimeUtils

/**
 * Main screen for the Volunteer role.
 *
 * Displays a list of available rescue batches and the current active rescue if one is claimed.
 *
 * @param onNavigateToProfile Callback to navigate to the user profile screen.
 * @param viewModel The [VolunteerViewModel] providing data and state for this screen.
 */
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeRescue != null) {
                        item {
                            ActiveRescueCard(
                                batch = activeRescue!!,
                                onCollect = { viewModel.collectRescue(activeRescue!!.id) },
                                onComplete = { viewModel.completeRescue(activeRescue!!.id) },
                                onOpenMap = { address -> openMap(context, address) },
                                isLoading = uiState is VolunteerState.Loading
                            )
                        }
                    }

                    item {
                        Text(
                            text = "LOTES DISPONIBLES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    if (availableBatches.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay más rescates disponibles.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(availableBatches) { batch ->
                            AvailableRescueItem(
                                batch = batch,
                                onClaim = { viewModel.claimRescue(batch.id) },
                                isLoading = uiState is VolunteerState.Loading
                            )
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

/**
 * A detailed card showing the current active rescue for the volunteer.
 *
 * Provides actions to open the map, contact the donor/recipient via WhatsApp or phone,
 * and mark the rescue as collected or delivered.
 *
 * @param batch The [RescueBatch] currently being handled.
 * @param onCollect Callback to mark the batch as collected from the donor.
 * @param onComplete Callback to mark the batch as delivered to the recipient.
 * @param onOpenMap Callback to open the navigation map for the given address.
 * @param isLoading Whether an operation is currently in progress.
 */
@Composable
fun ActiveRescueCard(
    batch: RescueBatch,
    onCollect: () -> Unit,
    onComplete: () -> Unit,
    onOpenMap: (String) -> Unit,
    isLoading: Boolean
) {
    val isCollected = batch.status == "COLLECTED"
    val cardColor = if (isCollected) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
    val accentColor = if (isCollected) Color(0xFF4CAF50) else Color(0xFF1976D2)
    val labelText = if (isCollected) "EN CAMINO A ENTREGA" else "EN CAMINO A RECOLECTAR"
    val addressTitle = if (isCollected) "DESTINO:" else "ORIGEN:"
    val addressValue = if (isCollected) (batch.recipientAddress ?: "No address set") else batch.donorAddress
    val phoneValue = if (isCollected) batch.recipientPhone else batch.donorPhone
    val actionText = if (isCollected) "CONFIRMAR ENTREGA" else "MARCAR COMO RECOLECTADO"
    val onAction = if (isCollected) onComplete else onCollect

    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, accentColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(labelText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = accentColor)
            Text(batch.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(addressTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(addressValue, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("HORARIO DE RECOLECCIÓN:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(batch.pickupWindow, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))

            val context = LocalContext.current
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOpenMap(addressValue) }, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("MAPA", fontSize = 11.sp)
                }

                if (!phoneValue.isNullOrBlank()) {
                    Button(
                        onClick = { openWhatsApp(context, phoneValue) }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Text("WHATSAPP", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { openDialer(context, phoneValue) }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("LLAMAR", fontSize = 11.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val isWithinWindow = TimeUtils.isCurrentTimeInWindow(batch.pickupWindow)
            val canAction = isCollected || isWithinWindow

            if (!isCollected && !isWithinWindow) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "FUERA DE HORARIO: Solo puedes recolectar durante el horario establecido por el donante.",
                        color = Color(0xFFC62828),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                enabled = !isLoading && canAction
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * A card representing a rescue batch available to be claimed by volunteers.
 *
 * @param batch The available [RescueBatch].
 * @param onClaim Callback to claim this rescue.
 * @param isLoading Whether an operation is currently in progress.
 */
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
            Text(text = "Horario: ${batch.pickupWindow}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1976D2))
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

private fun openWhatsApp(context: Context, phone: String) {
    try {
        val url = "https://api.whatsapp.com/send?phone=${phone.filter { it.isDigit() }}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to SMS if WhatsApp fails
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${phone}"))
        context.startActivity(intent)
    }
}

private fun openDialer(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone}"))
    context.startActivity(intent)
}

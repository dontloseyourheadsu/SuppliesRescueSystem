package com.udlap.suppliesrescuesystem.ui.donor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: RescueViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.cachedUser.collectAsState()
    val recipients by viewModel.recipients.collectAsState()
    val publishState by viewModel.publishState.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pickupWindow by remember { mutableStateOf("") }
    
    // Recipient Info (null means "Open to all")
    var selectedRecipientId by remember { mutableStateOf<String?>(null) }
    var selectedRecipientName by remember { mutableStateOf("OPEN TO ALL SHELTERS") }
    var selectedRecipientAddress by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(publishState) {
        if (publishState is PublishState.Success) {
            onNavigateBack()
            viewModel.resetPublishState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PUBLISH RESCUE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Donor Info (Read-only)
            Text("DONOR INFO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Organization: ${currentUser?.name ?: "Loading..."}", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Address: ${currentUser?.address ?: "N/A"}", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Batch Details
            Text("BATCH DETAILS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("What are you rescuing? (e.g. 50 Tacos)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity/Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = pickupWindow,
                        onValueChange = { pickupWindow = it },
                        label = { Text("Pickup Window (e.g. 6 PM - 8 PM)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recipient Selection
            Text("RECIPIENT (SHELTER)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedRecipientName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Option 1: Open to all
                    DropdownMenuItem(
                        text = { Text("OPEN TO ALL SHELTERS", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)) },
                        onClick = {
                            selectedRecipientId = null
                            selectedRecipientName = "OPEN TO ALL SHELTERS"
                            selectedRecipientAddress = ""
                            expanded = false
                        }
                    )
                    
                    HorizontalDivider()

                    // Option 2: Specific Recipients
                    if (recipients.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No shelters found", color = Color.Gray) },
                            onClick = { expanded = false },
                            enabled = false
                        )
                    } else {
                        recipients.forEach { recipient ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(recipient.name, fontWeight = FontWeight.Bold)
                                        Text(recipient.address ?: "No address", fontSize = 12.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedRecipientId = recipient.uid
                                    selectedRecipientName = recipient.name
                                    selectedRecipientAddress = recipient.address ?: ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedRecipientAddress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Recipient Address: $selectedRecipientAddress", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (publishState is PublishState.Error) {
                Text(
                    text = (publishState as PublishState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.publishBatchExtended(
                        title = title,
                        quantity = quantity,
                        pickupWindow = pickupWindow,
                        donorName = currentUser?.name ?: "",
                        donorAddress = currentUser?.address ?: "",
                        recipientId = selectedRecipientId ?: "", // Empty string if open
                        recipientName = selectedRecipientName,
                        recipientAddress = selectedRecipientAddress,
                        expiresAt = System.currentTimeMillis() + (4 * 60 * 60 * 1000)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = publishState !is PublishState.Loading && 
                        title.isNotBlank() && 
                        quantity.isNotBlank() && 
                        pickupWindow.isNotBlank()
            ) {
                if (publishState is PublishState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBLISH NOW", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

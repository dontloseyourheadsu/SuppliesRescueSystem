package com.udlap.suppliesrescuesystem.ui.donor

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen for donors to publish new food rescue batches.
 *
 * Collects information about the food (title, quantity, pickup window, expiration),
 * donor details, and allows selecting a recipient from a list of registered shelters.
 *
 * @param onNavigateBack Callback to return to the previous screen.
 * @param viewModel The [RescueViewModel] instance for managing publication and recipient data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: RescueViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pickupWindow by remember { mutableStateOf("") }
    var expirationHours by remember { mutableStateOf("4") }
    var donorName by remember { mutableStateOf("") }
    var donorAddress by remember { mutableStateOf("") }
    
    var selectedRecipientId by remember { mutableStateOf("") }
    var selectedRecipientName by remember { mutableStateOf("Select Recipient") }
    var recipientAddress by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }
    
    val publishState by viewModel.publishState.collectAsState()
    val recipients by viewModel.recipients.collectAsState()

    LaunchedEffect(publishState) {
        if (publishState is PublishState.Success) {
            viewModel.resetPublishState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PUBLISH BATCH") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Text("FOOD INFO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g., 20 rolls of bread)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Approximate Quantity/Weight") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = pickupWindow,
                        onValueChange = { pickupWindow = it },
                        label = { Text("Pickup Window (e.g., 8:00 - 10:00 PM)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = expirationHours,
                        onValueChange = { expirationHours = it },
                        label = { Text("Expires in (hours from now)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("MY INFO (DONOR)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    TextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text("Organization Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = donorAddress,
                        onValueChange = { donorAddress = it },
                        label = { Text("Organization Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("RECIPIENT INFO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedRecipientName, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            recipients.forEach { recipient ->
                                DropdownMenuItem(
                                    text = { Text(recipient.name) },
                                    onClick = {
                                        selectedRecipientId = recipient.uid
                                        selectedRecipientName = recipient.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    TextField(
                        value = recipientAddress,
                        onValueChange = { recipientAddress = it },
                        label = { Text("Recipient Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                }
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
                    val hours = expirationHours.toLongOrNull() ?: 4L
                    val expiresAt = System.currentTimeMillis() + (hours * 3600000L)
                    viewModel.publishBatchExtended(
                        title, quantity, pickupWindow, 
                        donorName, donorAddress, 
                        selectedRecipientId, selectedRecipientName, recipientAddress,
                        expiresAt
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = publishState !is PublishState.Loading && title.isNotBlank() && selectedRecipientId.isNotBlank()
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

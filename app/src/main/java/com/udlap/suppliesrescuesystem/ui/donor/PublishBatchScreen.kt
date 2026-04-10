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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: RescueViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pickupWindow by remember { mutableStateOf("") }
    var donorName by remember { mutableStateOf("") }
    var donorAddress by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var recipientAddress by remember { mutableStateOf("") }
    
    val publishState by viewModel.publishState.collectAsState()

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

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("LOCATIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    TextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text("Donor Name (e.g., Baker's Shop)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = donorAddress,
                        onValueChange = { donorAddress = it },
                        label = { Text("Donor Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Name (e.g., Hope Shelter)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
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
                    viewModel.publishBatchExtended(
                        title, quantity, pickupWindow, 
                        donorName, donorAddress, 
                        recipientName, recipientAddress
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = publishState !is PublishState.Loading && title.isNotBlank() && donorAddress.isNotBlank() && recipientAddress.isNotBlank()
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

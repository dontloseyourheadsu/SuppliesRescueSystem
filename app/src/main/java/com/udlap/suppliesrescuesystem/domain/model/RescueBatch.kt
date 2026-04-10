package com.udlap.suppliesrescuesystem.domain.model

import java.util.UUID

data class RescueBatch(
    val id: String = UUID.randomUUID().toString(),
    val donorId: String = "",
    val donorName: String = "",
    val donorAddress: String = "",
    val recipientName: String = "",
    val recipientAddress: String = "",
    val title: String = "",
    val quantity: String = "",
    val pickupWindow: String = "",
    val expiresAt: Long = 0, // Timestamp for automatic filtering
    val status: String = "AVAILABLE", // AVAILABLE, CLAIMED, DELIVERED
    val volunteerId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

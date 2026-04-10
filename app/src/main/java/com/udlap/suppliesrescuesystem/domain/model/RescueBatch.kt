package com.udlap.suppliesrescuesystem.domain.model

import java.util.UUID

data class RescueBatch(
    val id: String = UUID.randomUUID().toString(),
    val donorId: String = "",
    val title: String = "",
    val quantity: String = "",
    val pickupWindow: String = "", // e.g., "8:00 PM - 10:00 PM"
    val imageUrl: String? = null,
    val status: String = "AVAILABLE", // AVAILABLE, CLAIMED, DELIVERED
    val createdAt: Long = System.currentTimeMillis()
)

package com.udlap.suppliesrescuesystem.domain.model

import java.util.UUID

/**
 * Represents a food rescue batch in the system.
 *
 * This entity contains all information about a food donation, including the donor's details,
 * the recipient's details, the pickup window, and the current status of the rescue.
 *
 * @property id Unique identifier for the rescue batch.
 * @property donorId Unique identifier of the donor who published the batch.
 * @property donorName Name of the donor organization.
 * @property donorAddress Physical address where the food can be picked up.
 * @property recipientId Unique identifier of the intended recipient (shelter).
 * @property recipientName Name of the recipient organization.
 * @property recipientAddress Physical address of the recipient organization.
 * @property title Brief title or name for the food donation (e.g., "Leftover Bakery Items").
 * @property quantity Description of the amount of food (e.g., "3 boxes", "10 kg").
 * @property pickupWindow Time range during which the food can be collected (e.g., "5:00 PM - 7:00 PM").
 * @property expiresAt Timestamp in milliseconds when the food will no longer be available or safe to rescue.
 * @property status Current state of the rescue process (AVAILABLE, CLAIMED, DELIVERED, RECEIVED).
 * @property volunteerId Unique identifier of the volunteer who claimed the rescue, if any.
 * @property createdAt Timestamp in milliseconds when the batch was created.
 */
data class RescueBatch(
    val id: String = UUID.randomUUID().toString(),
    val donorId: String = "",
    val donorName: String = "",
    val donorAddress: String = "",
    val donorPhone: String = "",
    val recipientId: String? = null,
    val recipientName: String? = null,
    val recipientAddress: String? = null,
    val recipientPhone: String? = null,
    val title: String = "",
    val quantity: String = "",
    val pickupWindow: String = "",
    val expiresAt: Long = 0,
    val status: String = "AVAILABLE", // AVAILABLE, CLAIMED, DELIVERED, RECEIVED
    val volunteerId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

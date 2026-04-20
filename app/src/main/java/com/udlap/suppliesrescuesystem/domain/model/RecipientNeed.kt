package com.udlap.suppliesrescuesystem.domain.model

import java.util.UUID

/**
 * Represents a specific food or supply need posted by a recipient organization.
 * 
 * @property id Unique identifier for the need.
 * @property recipientId UID of the recipient organization.
 * @property recipientName Name of the recipient organization.
 * @property description Details of what is needed (e.g., "10kg of flour", "Fresh vegetables").
 * @property createdAt Timestamp of when the need was posted.
 * @property isFulfilled Whether this need has been met by a donation.
 */
data class RecipientNeed(
    val id: String = UUID.randomUUID().toString(),
    val recipientId: String = "",
    val recipientName: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFulfilled: Boolean = false
)

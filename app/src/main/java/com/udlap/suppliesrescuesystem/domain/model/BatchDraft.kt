package com.udlap.suppliesrescuesystem.domain.model

/**
 * Model representing a draft of a food rescue batch.
 *
 * This is used to persist unsaved data when a donor is filling out the publication form.
 */
data class BatchDraft(
    val title: String = "",
    val quantity: String = "",
    val pickupWindow: String = "",
    val recipientId: String = ""
)

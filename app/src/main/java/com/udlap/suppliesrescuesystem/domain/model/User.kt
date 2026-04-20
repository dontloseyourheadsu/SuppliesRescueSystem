package com.udlap.suppliesrescuesystem.domain.model

/**
 * Represents a user in the system, which can be a donor, volunteer, or recipient.
 *
 * @property uid Unique identifier provided by Firebase Auth.
 * @property email User's email address.
 * @property role Role of the user, determining their access and permissions (DONOR, VOLUNTEER, RECIPIENT).
 * @property name Human-readable name of the user, which can be an organization name or individual name.
 */
data class User(
    val uid: String,
    val email: String,
    val role: String, // DONOR, VOLUNTEER, RECIPIENT
    val name: String = "", // Organization or Individual Name
    val address: String? = null
)

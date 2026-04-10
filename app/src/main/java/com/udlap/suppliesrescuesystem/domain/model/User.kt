package com.udlap.suppliesrescuesystem.domain.model

data class User(
    val uid: String,
    val email: String,
    val role: String // DONOR, VOLUNTEER, RECIPIENT
)

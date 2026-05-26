package com.audine.dedalo.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isCurrentUser: Boolean = false
)

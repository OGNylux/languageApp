package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1, // Single profile
    val name: String,
    val nativeLanguage: String, // Language code like "en", "de", etc.
    val darkMode: Boolean = false // theme preference
)


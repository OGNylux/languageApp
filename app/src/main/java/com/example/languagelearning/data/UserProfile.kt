package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val nativeLanguage: String,
    val darkMode: Boolean = false
)


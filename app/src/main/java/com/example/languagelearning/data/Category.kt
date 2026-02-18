package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val foreignLanguage: String = "de", // Language you're learning from (e.g., "de" for German)
    val targetLanguage: String = "en"  // Language to translate to (e.g., "en" for English)
)


package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val foreignLanguage: String = "de",
    val targetLanguage: String = "en"
)


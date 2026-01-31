package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "translations", indices = [Index(value = ["source","sourceLang","targetLang"], unique = true)])
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val sourceLang: String,
    val targetLang: String,
    val translatedText: String,
    val timestamp: Long
)

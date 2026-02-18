package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val word: String,
    val translation: String? = null,
    val isBookmarked: Boolean = false,
    val incorrectCount: Int = 0, // Number of times answered incorrectly
    val lastIncorrectTimestamp: Long = 0 // When it was last answered incorrectly
)


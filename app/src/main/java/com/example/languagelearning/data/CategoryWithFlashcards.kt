package com.example.languagelearning.data

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithFlashcards(
    @Embedded val category: Category,
    @Relation(parentColumn = "id", entityColumn = "categoryId")
    val flashcards: List<Flashcard>
)


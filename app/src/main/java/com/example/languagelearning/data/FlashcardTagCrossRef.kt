package com.example.languagelearning.data

import androidx.room.Entity

@Entity(primaryKeys = ["flashcardId", "tagId"], tableName = "flashcard_tag_crossref")
data class FlashcardTagCrossRef(
    val flashcardId: Long,
    val tagId: Long
)


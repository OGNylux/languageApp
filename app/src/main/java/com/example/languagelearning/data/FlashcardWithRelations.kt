package com.example.languagelearning.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class FlashcardWithRelations(
    @Embedded val flashcard: Flashcard,
    @Relation(parentColumn = "id", entityColumn = "flashcardId")
    val examples: List<ExampleSentence>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(FlashcardTagCrossRef::class, parentColumn = "flashcardId", entityColumn = "tagId")
    )
    val tags: List<Tag>
)


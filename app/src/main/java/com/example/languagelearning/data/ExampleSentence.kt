package com.example.languagelearning.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "example_sentences",
    foreignKeys = [ForeignKey(entity = Flashcard::class, parentColumns = ["id"], childColumns = ["flashcardId"], onDelete = ForeignKey.CASCADE)]
)
data class ExampleSentence(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flashcardId: Long,
    val text: String
)


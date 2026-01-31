package com.example.languagelearning.data.dao

import androidx.room.*
import com.example.languagelearning.data.ExampleSentence
import kotlinx.coroutines.flow.Flow

@Dao
interface ExampleSentenceDao {
    @Query("SELECT * FROM example_sentences WHERE flashcardId = :flashcardId")
    fun getByFlashcard(flashcardId: Long): Flow<List<ExampleSentence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(example: ExampleSentence): Long

    @Delete
    suspend fun delete(example: ExampleSentence)

    @Query("DELETE FROM example_sentences WHERE flashcardId = :flashcardId")
    suspend fun deleteForFlashcard(flashcardId: Long)
}


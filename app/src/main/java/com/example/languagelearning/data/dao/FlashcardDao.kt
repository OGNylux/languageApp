package com.example.languagelearning.data.dao

import androidx.room.*
import com.example.languagelearning.data.Flashcard
import com.example.languagelearning.data.FlashcardTagCrossRef
import com.example.languagelearning.data.FlashcardWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId")
    fun getFlashcardsForCategory(categoryId: Long): Flow<List<Flashcard>>

    @Transaction
    @Query("SELECT * FROM flashcards WHERE id = :id")
    fun getFlashcardWithRelations(id: Long): Flow<FlashcardWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: Flashcard): Long

    @Update
    suspend fun update(flashcard: Flashcard)

    @Delete
    suspend fun delete(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: FlashcardTagCrossRef)

    @Query("DELETE FROM flashcard_tag_crossref WHERE flashcardId = :flashcardId")
    suspend fun clearTagsForFlashcard(flashcardId: Long)

    @Query("UPDATE flashcards SET translation = NULL WHERE translation LIKE '<%' OR translation LIKE '%<html%'")
    suspend fun clearHtmlTranslations()

    @Query("DELETE FROM flashcards WHERE categoryId = :categoryId")
    suspend fun deleteAllForCategory(categoryId: Long)
}

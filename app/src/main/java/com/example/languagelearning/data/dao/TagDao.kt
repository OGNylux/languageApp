package com.example.languagelearning.data.dao

import androidx.room.*
import com.example.languagelearning.data.Tag
import com.example.languagelearning.data.FlashcardTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT t.* FROM tags t INNER JOIN flashcard_tag_crossref c ON t.id = c.tagId WHERE c.flashcardId = :flashcardId")
    fun getTagsForFlashcard(flashcardId: Long): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: FlashcardTagCrossRef)

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?
}

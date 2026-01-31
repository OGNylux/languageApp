package com.example.languagelearning.data.dao

import androidx.room.*
import com.example.languagelearning.data.Category
import com.example.languagelearning.data.CategoryWithFlashcards
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Transaction
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryWithFlashcards(id: Long): Flow<CategoryWithFlashcards?>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

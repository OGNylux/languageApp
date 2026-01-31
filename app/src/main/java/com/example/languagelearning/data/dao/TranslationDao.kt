package com.example.languagelearning.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.languagelearning.data.TranslationEntity

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations WHERE source = :source AND sourceLang = :s AND targetLang = :t LIMIT 1")
    suspend fun get(source: String, s: String, t: String): TranslationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TranslationEntity)
}

package com.example.languagelearning.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.languagelearning.data.dao.CategoryDao
import com.example.languagelearning.data.dao.ExampleSentenceDao
import com.example.languagelearning.data.dao.FlashcardDao
import com.example.languagelearning.data.dao.TagDao
import com.example.languagelearning.data.dao.TranslationDao
import com.example.languagelearning.data.dao.UserProfileDao

@Database(
    entities = [Category::class, Flashcard::class, ExampleSentence::class, Tag::class, FlashcardTagCrossRef::class, TranslationEntity::class, UserProfile::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun exampleSentenceDao(): ExampleSentenceDao
    abstract fun tagDao(): TagDao
    abstract fun translationDao(): TranslationDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "language_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

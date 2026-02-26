package com.example.languagelearning.data

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.concurrent.TimeUnit

class LanguageRepository(
    private val db: AppDatabase,
    private val mlKit: MlKitTranslator? = null
) {
    // User Profile
    fun getUserProfile() = db.userProfileDao().getProfile()
    suspend fun getUserProfileSync(): UserProfile? = db.userProfileDao().getProfileSync()
    suspend fun insertOrUpdateProfile(profile: UserProfile) = db.userProfileDao().insert(profile)

    // Categories
    fun getAllCategories(): Flow<List<Category>> = db.categoryDao().getAllCategories()
    suspend fun insertCategory(category: Category): Long = db.categoryDao().insert(category)
    suspend fun updateCategory(category: Category) = db.categoryDao().update(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().delete(category)
    fun getCategoryWithFlashcards(id: Long) = db.categoryDao().getCategoryWithFlashcards(id)
    suspend fun getCategoryById(id: Long): Category? = db.categoryDao().getCategoryById(id)

    // Flashcards
    fun getFlashcardsForCategory(categoryId: Long) = db.flashcardDao().getFlashcardsForCategory(categoryId)
    fun getFlashcardWithRelations(id: Long) = db.flashcardDao().getFlashcardWithRelations(id)
    suspend fun insertFlashcard(flashcard: Flashcard): Long = db.flashcardDao().insert(flashcard)
    suspend fun updateFlashcard(flashcard: Flashcard) = db.flashcardDao().update(flashcard)

    suspend fun updateFlashcardWithDetails(flashcard: Flashcard, examples: List<String>, tags: List<String>) {
        db.withTransaction {
            db.flashcardDao().update(flashcard)

            db.exampleSentenceDao().deleteForFlashcard(flashcard.id)
            examples.forEach {
                db.exampleSentenceDao().insert(ExampleSentence(flashcardId = flashcard.id, text = it))
            }

            db.flashcardDao().clearTagsForFlashcard(flashcard.id)
            tags.forEach { tagName ->
                val tag = getOrCreateTagByName(tagName)
                db.flashcardDao().insertCrossRef(FlashcardTagCrossRef(flashcardId = flashcard.id, tagId = tag.id))
            }
        }
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) = db.flashcardDao().delete(flashcard)
    suspend fun deleteAllFlashcardsForCategory(categoryId: Long) = db.flashcardDao().deleteAllForCategory(categoryId)

    // Example sentences
    fun getExamplesForFlashcard(flashcardId: Long): Flow<List<ExampleSentence>> = db.exampleSentenceDao().getByFlashcard(flashcardId)
    suspend fun insertExample(example: ExampleSentence): Long = db.exampleSentenceDao().insert(example)
    suspend fun insertTagCrossRef(crossRef: FlashcardTagCrossRef) = db.flashcardDao().insertCrossRef(crossRef)
    fun getTagsForFlashcard(flashcardId: Long): Flow<List<Tag>> = db.tagDao().getTagsForFlashcard(flashcardId)

    suspend fun translate(
        word: String,
        sourceLang: String = "de",
        targetLang: String = "en"
    ): String = withTimeout(10_000L) {
        val mlKit = mlKit ?: throw Exception("ML Kit translator not available").also {
            Log.e("LanguageRepo", it.message!!)
        }

        try {
            val isReady = mlKit.prepareTranslator(sourceLang, targetLang, requireWifi = false)
            if (!isReady) throw Exception("ML Kit model not available for $sourceLang -> $targetLang")

            val translated = mlKit.translate(word)

            db.translationDao().insert(
                TranslationEntity(
                    source = word,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    translatedText = translated,
                    timestamp = System.currentTimeMillis()
                )
            )

            translated
        } catch (e: TimeoutCancellationException) {
            Log.e("LanguageRepo", "Translation timed out for '$word'")
            throw Exception("Translation timed out")
        } catch (e: Exception) {
            Log.e("LanguageRepo", "Translation failed for '$word': ${e.message}")
            throw e
        }
    }

    suspend fun getOrCreateTagByName(name: String): Tag {
        val dao = db.tagDao()
        val existing = dao.getTagByName(name)
        return existing ?: let {
            val id = dao.insert(Tag(name = name))
            Tag(id = id, name = name)
        }
    }
}

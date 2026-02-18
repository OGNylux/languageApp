package com.example.languagelearning.data

import android.util.Log
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
    suspend fun deleteFlashcard(flashcard: Flashcard) = db.flashcardDao().delete(flashcard)
    suspend fun deleteAllFlashcardsForCategory(categoryId: Long) = db.flashcardDao().deleteAllForCategory(categoryId)

    // Example sentences
    fun getExamplesForFlashcard(flashcardId: Long): Flow<List<ExampleSentence>> = db.exampleSentenceDao().getByFlashcard(flashcardId)
    suspend fun insertExample(example: ExampleSentence): Long = db.exampleSentenceDao().insert(example)
    suspend fun deleteExamplesForFlashcard(flashcardId: Long) = db.exampleSentenceDao().deleteForFlashcard(flashcardId)

    // Tags
    fun getAllTags(): Flow<List<Tag>> = db.tagDao().getAllTags()
    suspend fun insertTag(tag: Tag): Long = db.tagDao().insert(tag)
    suspend fun insertTagCrossRef(crossRef: FlashcardTagCrossRef) = db.flashcardDao().insertCrossRef(crossRef)
    suspend fun clearTagsForFlashcard(flashcardId: Long) = db.flashcardDao().clearTagsForFlashcard(flashcardId)
    fun getTagsForFlashcard(flashcardId: Long): Flow<List<Tag>> = db.tagDao().getTagsForFlashcard(flashcardId)

    suspend fun translate(word: String, sourceLang: String = "de", targetLang: String = "en"): String {
        val dao = db.translationDao()
        val cached = dao.get(word, sourceLang, targetLang)
        val ttlMs = TimeUnit.DAYS.toMillis(30)
        if (cached != null && System.currentTimeMillis() - cached.timestamp < ttlMs) {
            Log.d("LanguageRepo", "translate: cache hit for '$word' -> '${cached.translatedText}'")
            return cached.translatedText
        }

        if (mlKit == null) {
            val msg = "ML Kit translator not available"
            Log.e("LanguageRepo", msg)
            throw Exception(msg)
        }

        val TOTAL_TIMEOUT_MS = 10_000L

        try {
            val (resolvedSource, resolvedTarget) = try {
                var src = sourceLang
                // If source is 'auto', try to detect language first
                if (src.equals("auto", ignoreCase = true)) {
                    val detected = try { mlKit.detectLanguage(word) } catch (e: Exception) {
                        Log.w("LanguageRepo", "language detect threw: ${e.message}")
                        null
                    }
                    if (!detected.isNullOrBlank()) src = detected
                    Log.d("LanguageRepo", "translate: auto-detected source='$src' for word='$word'")
                }
                src to targetLang
            } catch (e: Exception) {
                Log.w("LanguageRepo", "resolve langs threw: ${e.message}")
                sourceLang to targetLang
            }

            val translated = try {
                withTimeout(TOTAL_TIMEOUT_MS) {
                    val prepared = try { mlKit.prepareTranslator(resolvedSource, resolvedTarget, requireWifi = false) } catch (e: Exception) {
                        Log.w("LanguageRepo", "prepareTranslator threw: ${e.message}")
                        false
                    }
                    if (!prepared) throw Exception("ML Kit model not available for $resolvedSource -> $resolvedTarget")

                    val out = mlKit.translate(word)
                    out
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("LanguageRepo", "translate: ML Kit operation timed out for '$word'")
                throw Exception("Translation timed out")
            }

            Log.d("LanguageRepo", "translate: ML Kit result for '$word' -> '$translated'")
            // cache and return
            dao.insert(TranslationEntity(source = word, sourceLang = resolvedSource, targetLang = resolvedTarget, translatedText = translated, timestamp = System.currentTimeMillis()))
            return translated
        } catch (e: Exception) {
            Log.e("LanguageRepo", "translate: ML Kit translation failed for '$word': ${e.message}")
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

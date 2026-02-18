package com.example.languagelearning.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelearning.data.Flashcard
import com.example.languagelearning.data.FlashcardWithRelations
import com.example.languagelearning.data.LanguageRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.firstOrNull

sealed class FlashcardEvent {
    data class Success(val id: Long) : FlashcardEvent()
    data class Error(val message: String) : FlashcardEvent()
}

class FlashcardsViewModel(private val repo: LanguageRepository, private val categoryId: Long) : ViewModel() {
    val flashcards: StateFlow<List<Flashcard>> = repo.getFlashcardsForCategory(categoryId).map { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val events = MutableSharedFlow<FlashcardEvent>(replay = 1)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _category = MutableStateFlow<com.example.languagelearning.data.Category?>(null)

    init {
        viewModelScope.launch {
            _category.value = repo.getCategoryById(categoryId)
        }
    }

    fun addFlashcard(word: String, exampleSentences: List<String>, tags: List<String>) = viewModelScope.launch {
        _isSaving.value = true
        val category = _category.value
        val sourceLang = category?.foreignLanguage ?: "de"
        val targetLang = category?.targetLanguage ?: "en"

        try {
            val translated = try {
                val TIMEOUT_MS = 15_000L
                val result = try {
                    withTimeout(TIMEOUT_MS) {
                        repo.translate(word, sourceLang, targetLang)
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e("FlashcardsVM", "translation timed out for '$word'")
                    throw Exception("Translation timed out")
                }
                Log.d("FlashcardsVM", "translation success: '$word' -> '$result'")
                result
            } catch (e: Exception) {
                Log.e("FlashcardsVM", "translation failed: ${e.message}", e)
                events.emit(FlashcardEvent.Error("Translation failed: ${e.message ?: "Unknown error"}. Flashcard not saved."))
                _isSaving.value = false
                return@launch
            }

            val flashcard = Flashcard(categoryId = categoryId, word = word, translation = translated)
            val id = repo.insertFlashcard(flashcard)
            Log.d("FlashcardsVM", "inserted flashcard id=$id with translation=${translated ?: "null"}")

            for (ex in exampleSentences) {
                repo.insertExample(com.example.languagelearning.data.ExampleSentence(flashcardId = id, text = ex))
            }
            for (tagName in tags) {
                val tag = repo.getOrCreateTagByName(tagName)
                repo.insertTagCrossRef(com.example.languagelearning.data.FlashcardTagCrossRef(flashcardId = id, tagId = tag.id))
            }

            events.emit(FlashcardEvent.Success(id))
        } catch (e: Exception) {
            //Log.e("FlashcardsVM", "addFlashcard failed", e)
            events.emit(FlashcardEvent.Error(e.message ?: "Failed to add flashcard"))
        } finally {
            _isSaving.value = false
        }
    }

    fun updateFlashcard(flashcard: Flashcard, fetchTranslation: Boolean = false) = viewModelScope.launch {
        val category = _category.value
        val sourceLang = category?.foreignLanguage ?: "de"
        val targetLang = category?.targetLanguage ?: "en"

        try {
            if (fetchTranslation) {
                Log.d("FlashcardsVM", "updateFlashcard: requesting translation for '${flashcard.word}'")
                _isSaving.value = true
            }

            val updated = if (fetchTranslation) {
                val t = try { repo.translate(flashcard.word, sourceLang, targetLang) } catch (e: Exception) {
                    Log.d("FlashcardsVM", "updateFlashcard: translation failed: ${e.message}")
                    flashcard.translation
                }
                flashcard.copy(translation = t)
            } else flashcard

            repo.updateFlashcard(updated)
            Log.d("FlashcardsVM", "updateFlashcard: updated flashcard id=${flashcard.id} translation='${updated.translation}'")
            events.emit(FlashcardEvent.Success(flashcard.id))
        } catch (e: Exception) {
            Log.e("FlashcardsVM", "updateFlashcard failed", e)
            events.emit(FlashcardEvent.Error(e.message ?: "Failed to update flashcard"))
        } finally {
            if (fetchTranslation) _isSaving.value = false
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) = viewModelScope.launch { repo.deleteFlashcard(flashcard) }

    fun deleteAllFlashcards() = viewModelScope.launch {
        try {
            repo.deleteAllFlashcardsForCategory(categoryId)
        } catch (e: Exception) {
            Log.e("FlashcardsVM", "deleteAllFlashcards failed", e)
        }
    }

    // Expose flow for single flashcard with relations
    fun getFlashcardWithRelationsFlow(id: Long) = repo.getFlashcardWithRelations(id)

    // helper to get single FlashcardWithRelations once (suspend)
    suspend fun getFlashcardWithRelationsOnce(id: Long): FlashcardWithRelations? {
        return repo.getFlashcardWithRelations(id).firstOrNull()
    }

    fun updateFlashcardWithRelations(flashcard: Flashcard, examples: List<String>, tags: List<String>, fetchTranslation: Boolean = false) = viewModelScope.launch {
        val category = _category.value
        val sourceLang = category?.foreignLanguage ?: "de"
        val targetLang = category?.targetLanguage ?: "en"

        try {
            _isSaving.value = true
            // possibly update translation first
            val updatedFlashcard = if (fetchTranslation) {
                val t = try { repo.translate(flashcard.word, sourceLang, targetLang) } catch (e: Exception) { flashcard.translation }
                flashcard.copy(translation = t)
            } else flashcard

            repo.updateFlashcard(updatedFlashcard)
            // replace examples
            repo.deleteExamplesForFlashcard(flashcard.id)
            for (ex in examples) repo.insertExample(com.example.languagelearning.data.ExampleSentence(flashcardId = flashcard.id, text = ex))
            // replace tags
            repo.clearTagsForFlashcard(flashcard.id)
            for (tagName in tags) {
                val tag = repo.getOrCreateTagByName(tagName)
                repo.insertTagCrossRef(com.example.languagelearning.data.FlashcardTagCrossRef(flashcardId = flashcard.id, tagId = tag.id))
            }

            events.emit(FlashcardEvent.Success(flashcard.id))
        } catch (e: Exception) {
            Log.e("FlashcardsVM", "updateFlashcardWithRelations failed", e)
            events.emit(FlashcardEvent.Error(e.message ?: "Failed to update flashcard"))
        } finally {
            _isSaving.value = false
        }
    }
}

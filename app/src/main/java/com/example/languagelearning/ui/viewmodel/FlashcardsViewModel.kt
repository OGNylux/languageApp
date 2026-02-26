package com.example.languagelearning.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelearning.data.ExampleSentence
import com.example.languagelearning.data.Flashcard
import com.example.languagelearning.data.FlashcardTagCrossRef
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
        val category = _category.value ?: return@launch // Guard clause if no category selected

        try {
            val translated = withTimeout(15_000L) {
                repo.translate(word, category.foreignLanguage, category.targetLanguage)
            }

            val flashcard = Flashcard(categoryId = categoryId, word = word, translation = translated)
            val id = repo.insertFlashcard(flashcard)

            exampleSentences.forEach { text ->
                repo.insertExample(ExampleSentence(flashcardId = id, text = text))
            }

            tags.forEach { tagName ->
                val tag = repo.getOrCreateTagByName(tagName)
                repo.insertTagCrossRef(FlashcardTagCrossRef(flashcardId = id, tagId = tag.id))
            }

            events.emit(FlashcardEvent.Success(id))
        } catch (e: Exception) {
            val errorMsg = if (e is TimeoutCancellationException) "Translation timed out" else e.message
            Log.e("FlashcardsVM", "Failed to add flashcard: $errorMsg", e)
            events.emit(FlashcardEvent.Error(errorMsg ?: "Unknown error"))
        } finally {
            _isSaving.value = false
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) = viewModelScope.launch { repo.deleteFlashcard(flashcard) }

    fun getFlashcardWithRelationsFlow(id: Long) = repo.getFlashcardWithRelations(id)

    fun updateFlashcardWithRelations(flashcard: Flashcard, examples: List<String>, tags: List<String>, fetchTranslation: Boolean = false) = viewModelScope.launch {
        val category = _category.value
        val sourceLang = category?.foreignLanguage ?: "de"
        val targetLang = category?.targetLanguage ?: "en"

        try {
            _isSaving.value = true
            var updatedFlashcard = flashcard

            if (fetchTranslation) {
                val result = try {
                    repo.translate(flashcard.word, sourceLang, targetLang)
                } catch (e: Exception) {
                    flashcard.translation
                }
                updatedFlashcard = flashcard.copy(translation = result)
            }

            repo.updateFlashcardWithDetails(updatedFlashcard, examples, tags)

            events.emit(FlashcardEvent.Success(flashcard.id))
        } catch (e: Exception) {
            Log.e("FlashcardsVM", "updateFlashcardWithRelations failed", e)
            events.emit(FlashcardEvent.Error(e.message ?: "Failed to update flashcard"))
        } finally {
            _isSaving.value = false
        }
    }
}

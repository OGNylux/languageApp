package com.example.languagelearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelearning.data.Category
import com.example.languagelearning.data.LanguageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CategoryEvent {
    data class Success(val id: Long) : CategoryEvent()
    data class Error(val message: String) : CategoryEvent()
}

class CategoriesViewModel(private val repo: LanguageRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> = repo.getAllCategories().map { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    // Use replay=1 so UI collectors don't miss the last emitted event
    val events = MutableSharedFlow<CategoryEvent>(replay = 1)

    fun addCategory(name: String, color: Int) = viewModelScope.launch {
        _isSaving.value = true
        try {
            val id = repo.insertCategory(Category(name = name, color = color))
            events.emit(CategoryEvent.Success(id))
        } catch (e: Exception) {
            events.emit(CategoryEvent.Error(e.message ?: "Failed to add category"))
        } finally {
            _isSaving.value = false
        }
    }

    fun updateCategory(id: Long, name: String, color: Int) = viewModelScope.launch {
        _isSaving.value = true
        try {
            repo.updateCategory(Category(id = id, name = name, color = color))
            events.emit(CategoryEvent.Success(id))
        } catch (e: Exception) {
            events.emit(CategoryEvent.Error(e.message ?: "Failed to update category"))
        } finally {
            _isSaving.value = false
        }
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repo.deleteCategory(category)
        // emit success after deletion so edit screen can react
        events.emit(CategoryEvent.Success(category.id))
    }

    fun deleteAllFlashcardsForCategory(categoryId: Long) = viewModelScope.launch {
        try {
            repo.deleteAllFlashcardsForCategory(categoryId)
        } catch (e: Exception) {
            events.emit(CategoryEvent.Error(e.message ?: "Failed to delete flashcards"))
        }
    }

    // suspend helper to load a category by id (call from composable LaunchedEffect)
    suspend fun getCategoryById(id: Long): Category? = repo.getCategoryById(id)

    // Provide a flow for a single category with relations to be observed in UI
    fun getCategoryFlow(id: Long) = repo.getCategoryWithFlashcards(id)
}

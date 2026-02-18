package com.example.languagelearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelearning.data.LanguageRepository
import com.example.languagelearning.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: LanguageRepository) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserProfile().collect { profile ->
                _profile.value = profile
            }
        }
    }

    fun createProfile(name: String, nativeLanguage: String, darkMode: Boolean = false) {
        viewModelScope.launch {
            val profile = UserProfile(id = 1, name = name, nativeLanguage = nativeLanguage, darkMode = darkMode)
            repository.insertOrUpdateProfile(profile)
        }
    }

    fun updateProfile(name: String, nativeLanguage: String, darkMode: Boolean = false) {
        viewModelScope.launch {
            val profile = UserProfile(id = 1, name = name, nativeLanguage = nativeLanguage, darkMode = darkMode)
            repository.insertOrUpdateProfile(profile)
        }
    }
}

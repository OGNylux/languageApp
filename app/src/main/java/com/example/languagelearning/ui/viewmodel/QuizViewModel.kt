package com.example.languagelearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelearning.data.Flashcard
import com.example.languagelearning.data.LanguageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class QuizMode {
    RANDOM, BOOKMARKED, RECALL
}

data class QuizQuestion(
    val flashcard: Flashcard,
    val options: List<String>,
    val correctAnswer: String
)

data class QuizState(
    val currentQuestion: QuizQuestion? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val selectedAnswer: String? = null,
    val hasAnswered: Boolean = false,
    val isCorrect: Boolean = false
)

class QuizViewModel(
    private val repository: LanguageRepository,
    private val categoryId: Long,
    private val mode: QuizMode
) : ViewModel() {

    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private var allQuestions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            val flashcards = when (mode) {
                QuizMode.RANDOM -> repository.getFlashcardsForCategory(categoryId).first()
                QuizMode.BOOKMARKED -> repository.getFlashcardsForCategory(categoryId).first().filter { it.isBookmarked }
                QuizMode.RECALL -> repository.getFlashcardsForCategory(categoryId).first().filter { it.incorrectCount > 0 }
            }.shuffled()

            if (flashcards.isEmpty()) {
                _quizState.value = QuizState(isFinished = true, totalQuestions = 0)
                return@launch
            }

            val allTranslations = repository.getFlashcardsForCategory(categoryId).first()
                .mapNotNull { it.translation }
                .distinct()

            allQuestions = flashcards.take(20).map { flashcard ->
                val correctAnswer = flashcard.translation ?: ""
                val wrongOptions = allTranslations.filter { it != correctAnswer }.shuffled().take(3)
                val options = (wrongOptions + correctAnswer).shuffled()
                QuizQuestion(flashcard, options, correctAnswer)
            }

            currentIndex = 0
            showNextQuestion()
        }
    }

    private fun showNextQuestion() {
        if (currentIndex < allQuestions.size) {
            _quizState.value = QuizState(
                currentQuestion = allQuestions[currentIndex],
                currentQuestionIndex = currentIndex + 1,
                totalQuestions = allQuestions.size,
                score = _quizState.value.score
            )
        } else {
            _quizState.value = _quizState.value.copy(isFinished = true)
        }
    }

    fun selectAnswer(answer: String) {
        val current = _quizState.value.currentQuestion ?: return
        val isCorrect = answer == current.correctAnswer

        _quizState.value = _quizState.value.copy(
            selectedAnswer = answer,
            hasAnswered = true,
            isCorrect = isCorrect,
            score = if (isCorrect) _quizState.value.score + 1 else _quizState.value.score
        )

        // Update flashcard stats
        viewModelScope.launch {
            if (!isCorrect) {
                val flashcard = current.flashcard
                repository.updateFlashcard(
                    flashcard.copy(
                        incorrectCount = flashcard.incorrectCount + 1,
                        lastIncorrectTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun nextQuestion() {
        viewModelScope.launch {
            _quizState.value = _quizState.value.copy(
                hasAnswered = false
            )

            delay(200)

            currentIndex++
            _quizState.value = _quizState.value.copy(
                selectedAnswer = null,
                isCorrect = false
            )
            showNextQuestion()
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val current = _quizState.value.currentQuestion ?: return@launch
            val flashcard = current.flashcard
            val updated = flashcard.copy(isBookmarked = !flashcard.isBookmarked)
            repository.updateFlashcard(updated)

            _quizState.value = _quizState.value.copy(
                currentQuestion = current.copy(flashcard = updated)
            )
        }
    }

    fun restartQuiz() {
        currentIndex = 0
        _quizState.value = QuizState()
        loadQuiz()
    }
}


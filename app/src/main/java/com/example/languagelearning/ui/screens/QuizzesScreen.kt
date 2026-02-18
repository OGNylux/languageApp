package com.example.languagelearning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagelearning.data.Category
import com.example.languagelearning.data.LanguageRepository
import kotlinx.coroutines.flow.first

@Composable
fun QuizzesScreen(
    repository: LanguageRepository,
    onQuizStart: (Long, String) -> Unit // categoryId, mode
) {
    val categories by repository.getAllCategories().collectAsState(initial = emptyList())

    // Group categories by language pair
    val groupedCategories = categories.groupBy {
        "${it.foreignLanguage} → ${it.targetLanguage}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            "Quizzes",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedCategories.forEach { (languagePair, categoriesInGroup) ->
                item {
                    Text(
                        text = languagePair,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(categoriesInGroup) { category ->
                    CategoryQuizCard(
                        category = category,
                        repository = repository,
                        onQuizStart = onQuizStart
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryQuizCard(
    category: Category,
    repository: LanguageRepository,
    onQuizStart: (Long, String) -> Unit
) {
    var totalCount by remember { mutableStateOf(0) }
    var bookmarkedCount by remember { mutableStateOf(0) }
    var recallCount by remember { mutableStateOf(0) }

    LaunchedEffect(category.id) {
        val flashcards = repository.getFlashcardsForCategory(category.id).first()
        totalCount = flashcards.size
        bookmarkedCount = flashcards.count { it.isBookmarked }
        recallCount = flashcards.count { it.incorrectCount > 0 }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(category.color), RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$totalCount flashcards",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizModeButton(
                    text = "Random",
                    count = totalCount,
                    enabled = totalCount > 0,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuizStart(category.id, "RANDOM") }
                )
                QuizModeButton(
                    text = "Bookmarked",
                    count = bookmarkedCount,
                    enabled = bookmarkedCount > 0,
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f),
                    onClick = { onQuizStart(category.id, "BOOKMARKED") }
                )
                QuizModeButton(
                    text = "Recall",
                    count = recallCount,
                    enabled = recallCount > 0,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.weight(1f),
                    onClick = { onQuizStart(category.id, "RECALL") }
                )
            }
        }
    }
}

@Composable
fun QuizModeButton(
    text: String,
    count: Int,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) color else color.copy(alpha = 0.3f),
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}


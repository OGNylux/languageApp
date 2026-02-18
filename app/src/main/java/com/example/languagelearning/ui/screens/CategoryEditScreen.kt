package com.example.languagelearning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.languagelearning.ui.viewmodel.CategoriesViewModel
import com.example.languagelearning.ui.viewmodel.CategoryEvent
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.languagelearning.LanguageApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(vm: CategoriesViewModel, categoryId: Long, onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as LanguageApp
    val repo = application.repository

    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0xFFFF8FAB.toInt()) }
    var foreignLanguage by remember { mutableStateOf("de") }
    var targetLanguage by remember { mutableStateOf("en") }
    var showForeignLangMenu by remember { mutableStateOf(false) }
    var showTargetLangMenu by remember { mutableStateOf(false) }

    val isSaving by vm.isSaving.collectAsState(initial = false)

    val languages = listOf(
        "en" to "English",
        "de" to "Deutsch",
        "es" to "Español",
        "fr" to "Français",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "ja" to "日本語",
        "ko" to "한국어",
        "zh" to "中文"
    )

    LaunchedEffect(Unit) {
        repo?.getUserProfileSync()?.let { profile ->
            targetLanguage = profile.nativeLanguage
        }
    }

    val colors = listOf(
        0xFFFFB5BA.toInt(),
        0xFFFFD6A5.toInt(),
        0xFFFDFFB6.toInt(),
        0xFFCAFFBF.toInt(),
        0xFFA0C4FF.toInt(),
        0xFFBDB2FF.toInt(),
        0xFFFFC6FF.toInt()
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isEditing = categoryId != 0L

    val categoryFlow = remember(categoryId) { vm.getCategoryFlow(categoryId) }
    val categoryWithFlashcards by categoryFlow.collectAsState(initial = null)

    LaunchedEffect(categoryWithFlashcards) {
        categoryWithFlashcards?.let { existing ->
            name = existing.category.name
            selectedColor = existing.category.color
            foreignLanguage = existing.category.foreignLanguage
            targetLanguage = existing.category.targetLanguage
        }
    }

    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.events.collect { ev ->
            when (ev) {
                is CategoryEvent.Success -> onBack()
                is CategoryEvent.Error -> scope.launch { snackbarHostState.showSnackbar(ev.message) }
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete category") },
            text = { Text("Are you sure you want to delete this category and all its flashcards? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val cat = categoryWithFlashcards?.category
                    if (cat != null) {
                        vm.deleteCategory(cat)
                        showDeleteDialog.value = false
                        onBack()
                    } else {
                        showDeleteDialog.value = false
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        text = if (isEditing) "Edit Category" else "Create Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isEditing) {
                        Button(
                            onClick = { showDeleteDialog.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Delete category")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.deleteAllFlashcardsForCategory(categoryId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Clear all flashcards")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && !isSaving) {
                                if (isEditing) vm.updateCategory(categoryId, name, selectedColor, foreignLanguage, targetLanguage)
                                else vm.addCategory(name, selectedColor, foreignLanguage, targetLanguage)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Category Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g., German Vocabulary") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Foreign Language (Learning From)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = showForeignLangMenu,
                onExpandedChange = { showForeignLangMenu = it }
            ) {
                OutlinedTextField(
                    value = languages.find { it.first == foreignLanguage }?.second ?: "Deutsch",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showForeignLangMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = showForeignLangMenu,
                    onDismissRequest = { showForeignLangMenu = false }
                ) {
                    languages.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                foreignLanguage = code
                                showForeignLangMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Target Language (Translate To)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = showTargetLangMenu,
                onExpandedChange = { showTargetLangMenu = it }
            ) {
                OutlinedTextField(
                    value = languages.find { it.first == targetLanguage }?.second ?: "English",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTargetLangMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = showTargetLangMenu,
                    onDismissRequest = { showTargetLangMenu = false }
                ) {
                    languages.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                targetLanguage = code
                                showTargetLangMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Pick a Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(
                                width = if (selectedColor == c) 4.dp else 0.dp,
                                color = if (selectedColor == c) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = !isSaving) { selectedColor = c }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Preview",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name.ifBlank { "Category Name" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

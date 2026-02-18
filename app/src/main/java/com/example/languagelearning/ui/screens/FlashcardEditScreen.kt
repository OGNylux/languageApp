package com.example.languagelearning.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.languagelearning.ui.viewmodel.FlashcardEvent
import com.example.languagelearning.ui.viewmodel.FlashcardsViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun FlashcardEditScreen(vm: FlashcardsViewModel, flashcardId: Long, categoryId: Long, onBack: () -> Unit) {
    val isEditing = flashcardId != 0L
    val isSaving by vm.isSaving.collectAsState(initial = false)

    var word by remember { mutableStateOf("") }
    val exampleInputs = remember { mutableStateListOf<String>() }
    val tagInputs = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Prefill when editing by collecting flow
    if (isEditing) {
        val flow = remember(flashcardId) { vm.getFlashcardWithRelationsFlow(flashcardId) }
        val rel by flow.collectAsState(initial = null)
        LaunchedEffect(rel) {
            rel?.let {
                word = it.flashcard.word
                exampleInputs.clear(); exampleInputs.addAll(it.examples.map { ex -> ex.text })
                tagInputs.clear(); tagInputs.addAll(it.tags.map { t -> t.name })
            }
        }
    } else {
        LaunchedEffect(Unit) {
            if (exampleInputs.isEmpty()) exampleInputs.add("")
            if (tagInputs.isEmpty()) tagInputs.add("")
        }
    }

    LaunchedEffect(vm) {
        vm.events.collect { ev ->
            when (ev) {
                is FlashcardEvent.Success -> {
                    Log.d("FlashcardEdit", "Flashcard saved id=${ev.id}")
                    // Navigate back immediately, show a non-blocking snackbar in background
                    onBack()
                    scope.launch {
                        snackbarHostState.showSnackbar("Flashcard saved! ✨")
                    }
                }
                is FlashcardEvent.Error -> {
                    scope.launch { snackbarHostState.showSnackbar(ev.message) }
                }
            }
        }
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        text = if (isEditing) "Edit Flashcard" else "Create Flashcard",
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
                    Button(
                        onClick = {
                            if (word.isNotBlank() && !isSaving) {
                                val examples = exampleInputs.map { it.trim() }.filter { it.isNotBlank() }
                                val tags = tagInputs.map { it.trim() }.filter { it.isNotBlank() }
                                if (isEditing) vm.updateFlashcardWithRelations(
                                    com.example.languagelearning.data.Flashcard(id = flashcardId, categoryId = categoryId, word = word, translation = null),
                                    examples, tags, true
                                )
                                else vm.addFlashcard(word, examples, tags)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Word input
            Text(
                text = "Word",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                placeholder = { Text("Enter the word to learn") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Example sentences
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Example Sentences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            exampleInputs.forEachIndexed { idx, value ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = value,
                            onValueChange = { exampleInputs[idx] = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Example sentence...") },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        )
                        if (idx == 0) {
                            IconButton(onClick = { exampleInputs.add("") }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            IconButton(onClick = { exampleInputs.removeAt(idx) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tags
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            tagInputs.forEachIndexed { idx, value ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = value,
                            onValueChange = { tagInputs[idx] = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Tag name...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        )
                        if (idx == 0) {
                            IconButton(onClick = { tagInputs.add("") }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            IconButton(onClick = { tagInputs.removeAt(idx) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for scroll
        }
    }
}

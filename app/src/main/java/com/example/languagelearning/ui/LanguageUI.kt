package com.example.languagelearning.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.languagelearning.LanguageApp
import com.example.languagelearning.ui.viewmodel.CategoriesViewModel
import com.example.languagelearning.ui.viewmodel.FlashcardsViewModel

enum class Routes(val route: String) {
    Categories("categories"),
    CategoryEdit("category/edit/{categoryId}"),
    Flashcards("flashcards/{categoryId}"),
    FlashcardDetail("flashcard/{flashcardId}"),
    FlashcardEdit("flashcard/edit/{flashcardId}/{categoryId}")
}

@Composable
fun LanguageAppUI(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val application = LocalContext.current.applicationContext as LanguageApp
    val repo = application.repository

    // If repository isn't ready yet, show a loading indicator instead of blocking.
    if (repo == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Non-null repository from here on
    val safeRepo = repo

    NavHost(navController = navController, startDestination = Routes.Categories.route, modifier = modifier) {
        composable(Routes.Categories.route) {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CategoriesViewModel(safeRepo) as T
                }
            }
            val vm: CategoriesViewModel = viewModel(factory = factory)
            com.example.languagelearning.ui.screens.CategoriesScreen(vm = vm, onOpenCategory = { id -> navController.navigate("flashcards/$id") }, onCreate = { navController.navigate("category/edit/0") })
        }

        composable(Routes.Flashcards.route, listOf(navArgument("categoryId") { type = NavType.LongType })) { backStack ->
            val id = backStack.arguments?.getLong("categoryId") ?: 0L
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FlashcardsViewModel(safeRepo, id) as T
                }
            }
            val vm: FlashcardsViewModel = viewModel(factory = factory)

            var categoryName by remember { mutableStateOf("Flashcards") }
            LaunchedEffect(id) {
                safeRepo.getCategoryById(id)?.let { categoryName = it.name }
            }

            com.example.languagelearning.ui.screens.FlashcardsScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpen = { fid -> navController.navigate("flashcard/$fid") },
                onCreate = { navController.navigate("flashcard/edit/0/$id") },
                onEditCategory = { navController.navigate("category/edit/$id") },
                onEdit = { fid -> navController.navigate("flashcard/edit/$fid/$id") },
                categoryName = categoryName
            )
        }

        composable(Routes.FlashcardDetail.route, listOf(navArgument("flashcardId") { type = NavType.LongType })) { backStack ->
            val fid = backStack.arguments?.getLong("flashcardId") ?: 0L
            com.example.languagelearning.ui.screens.FlashcardDetailScreen(repository = safeRepo, flashcardId = fid, onBack = { navController.popBackStack() })
        }

        composable(Routes.FlashcardEdit.route, listOf(navArgument("flashcardId") { type = NavType.LongType }, navArgument("categoryId") { type = NavType.LongType })) { backStack ->
            val fid = backStack.arguments?.getLong("flashcardId") ?: 0L
            val catId = backStack.arguments?.getLong("categoryId") ?: 0L
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FlashcardsViewModel(safeRepo, catId) as T
                }
            }
            val vm: FlashcardsViewModel = viewModel(factory = factory)
            com.example.languagelearning.ui.screens.FlashcardEditScreen(vm = vm, flashcardId = fid, categoryId = catId, onBack = {
                // Try to pop back to the flashcards list for this category if it exists in the back stack.
                val targetRoute = "flashcards/$catId"
                val popped = navController.popBackStack(targetRoute, false)
                if (!popped) {
                    // If not found in backstack, navigate to it directly (replace behavior minimized by launchSingleTop)
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                        // keep categories on back stack
                        popUpTo(Routes.Categories.route) { inclusive = false }
                    }
                }
            })
        }

        composable(Routes.CategoryEdit.route, listOf(navArgument("categoryId") { type = NavType.LongType })) { backStack ->
            val id = backStack.arguments?.getLong("categoryId") ?: 0L
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CategoriesViewModel(safeRepo) as T
                }
            }
            val vm: CategoriesViewModel = viewModel(factory = factory)
            com.example.languagelearning.ui.screens.CategoryEditScreen(vm = vm, categoryId = id, onBack = {
                navController.navigate(Routes.Categories.route) {
                    popUpTo(Routes.Categories.route) { inclusive = false }
                }
            })
        }
    }
}

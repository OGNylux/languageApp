package com.example.languagelearning.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.languagelearning.LanguageApp
import com.example.languagelearning.ui.viewmodel.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.languagelearning.ui.screens.CategoriesScreen
import com.example.languagelearning.ui.screens.CategoryEditScreen
import com.example.languagelearning.ui.screens.FlashcardDetailScreen
import com.example.languagelearning.ui.screens.FlashcardEditScreen
import com.example.languagelearning.ui.screens.FlashcardsScreen
import com.example.languagelearning.ui.screens.ProfileScreen
import com.example.languagelearning.ui.screens.QuizzesScreen
import com.example.languagelearning.ui.theme.LanguageLearningTheme

enum class Routes(val route: String) {
    Categories("categories"),
    Quizzes("quizzes"),
    Profile("profile"),
    Quiz("quiz/{categoryId}/{mode}"),
    CategoryEdit("category/edit/{categoryId}"),
    Flashcards("flashcards/{categoryId}"),
    FlashcardDetail("flashcard/{flashcardId}"),
    FlashcardEdit("flashcard/edit/{flashcardId}/{categoryId}")
}

@Composable
fun LanguageAppUI(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val application = LocalContext.current.applicationContext as LanguageApp
    val repo = application.repository

    // If repository isnt ready show loading
    if (repo == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val profileFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repo) as T
        }
    }
    val profileViewModel: ProfileViewModel = viewModel(factory = profileFactory)
    val profile by profileViewModel.profile.collectAsState()

    val darkPref = profile?.darkMode ?: isSystemInDarkTheme()

    LanguageLearningTheme(darkTheme = darkPref) {
        if (profile == null) {
            com.example.languagelearning.ui.screens.ProfileCreateScreen(
                vm = profileViewModel,
                onProfileCreated = {}
            )
            return@LanguageLearningTheme
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val bottomBarRoutes = listOf(Routes.Categories.route, Routes.Quizzes.route, Routes.Profile.route)
        val showBottomBar = currentRoute in bottomBarRoutes

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Categories") },
                            label = { Text("Categories") },
                            selected = currentRoute == Routes.Categories.route,
                            onClick = {
                                navController.navigate(Routes.Categories.route) {
                                    popUpTo(Routes.Categories.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Star, contentDescription = "Quizzes") },
                            label = { Text("Quizzes") },
                            selected = currentRoute == Routes.Quizzes.route,
                            onClick = {
                                navController.navigate(Routes.Quizzes.route) {
                                    popUpTo(Routes.Categories.route)
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentRoute == Routes.Profile.route,
                            onClick = {
                                navController.navigate(Routes.Profile.route) {
                                    popUpTo(Routes.Categories.route)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.Categories.route,
                modifier = modifier.padding(paddingValues)
            ) {

                composable(Routes.Categories.route) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CategoriesViewModel(repo) as T
                        }
                    }
                    val vm: CategoriesViewModel = viewModel(factory = factory)
                    CategoriesScreen(vm = vm, onOpenCategory = { id -> navController.navigate("flashcards/$id") }, onCreate = { navController.navigate("category/edit/0") })
                }

                composable(Routes.Quizzes.route) {
                    QuizzesScreen(
                        repository = repo,
                        onQuizStart = { categoryId, mode ->
                            navController.navigate("quiz/$categoryId/$mode")
                        }
                    )
                }

                composable(Routes.Profile.route) {
                    ProfileScreen(vm = profileViewModel)
                }

                composable(Routes.Quiz.route, listOf(
                    navArgument("categoryId") { type = NavType.LongType },
                    navArgument("mode") { type = NavType.StringType }
                )) { backStack ->
                    val categoryId = backStack.arguments?.getLong("categoryId") ?: 0L
                    val modeStr = backStack.arguments?.getString("mode") ?: "RANDOM"
                    val mode = when (modeStr) {
                        "BOOKMARKED" -> QuizMode.BOOKMARKED
                        "RECALL" -> QuizMode.RECALL
                        else -> QuizMode.RANDOM
                    }

                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return QuizViewModel(repo, categoryId, mode) as T
                        }
                    }
                    val vm: QuizViewModel = viewModel(factory = factory)
                    com.example.languagelearning.ui.screens.QuizScreen(vm = vm, onBack = { navController.popBackStack() })
                }

                composable(Routes.Flashcards.route, listOf(navArgument("categoryId") { type = NavType.LongType })) { backStack ->
                    val id = backStack.arguments?.getLong("categoryId") ?: 0L
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FlashcardsViewModel(repo, id) as T
                        }
                    }
                    val vm: FlashcardsViewModel = viewModel(factory = factory)

                    var categoryName by remember { mutableStateOf("Flashcards") }
                    LaunchedEffect(id) {
                        repo.getCategoryById(id)?.let { categoryName = it.name }
                    }

                    FlashcardsScreen(
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
                    FlashcardDetailScreen(repository = repo, flashcardId = fid, onBack = { navController.popBackStack() })
                }

                composable(Routes.FlashcardEdit.route, listOf(navArgument("flashcardId") { type = NavType.LongType }, navArgument("categoryId") { type = NavType.LongType })) { backStack ->
                    val fid = backStack.arguments?.getLong("flashcardId") ?: 0L
                    val catId = backStack.arguments?.getLong("categoryId") ?: 0L
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FlashcardsViewModel(repo, catId) as T
                        }
                    }
                    val vm: FlashcardsViewModel = viewModel(factory = factory)
                    FlashcardEditScreen(vm = vm, flashcardId = fid, categoryId = catId, onBack = {
                        val targetRoute = "flashcards/$catId"
                        val popped = navController.popBackStack(targetRoute, false)
                        if (!popped) {
                            navController.navigate(targetRoute) {
                                launchSingleTop = true
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
                            return CategoriesViewModel(repo) as T
                        }
                    }
                    val vm: CategoriesViewModel = viewModel(factory = factory)
                    CategoryEditScreen(vm = vm, categoryId = id, onBack = {
                        navController.navigate(Routes.Categories.route) {
                            popUpTo(Routes.Categories.route) { inclusive = false }
                        }
                    })
                }
            }
        }
    }
}

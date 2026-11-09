package com.aetnox.cooking.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aetnox.cooking.data.database.AppDatabase
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.ui.screen.BackupScreen
import com.aetnox.cooking.ui.screen.CookModeScreen
import com.aetnox.cooking.ui.screen.RecipeEditScreen
import com.aetnox.cooking.ui.screen.RecipeListScreen
import com.aetnox.cooking.ui.viewmodel.BackupViewModel
import com.aetnox.cooking.ui.viewmodel.CookModeViewModel
import com.aetnox.cooking.ui.viewmodel.RecipeEditViewModel
import com.aetnox.cooking.ui.viewmodel.RecipeListViewModel

object CookingRoutes {
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_EDIT = "recipe_edit"
    const val COOK_MODE = "cook_mode"
    const val BACKUP = "backup"
}

@Composable
fun CookingNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val repository = RecipeRepository(database.recipeDao())

    NavHost(
        navController = navController,
        startDestination = CookingRoutes.RECIPE_LIST,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(CookingRoutes.RECIPE_LIST) {
            val viewModel: RecipeListViewModel = viewModel(
                factory = RecipeListViewModel.Factory(repository)
            )
            RecipeListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { recipeId ->
                    if (recipeId != null && recipeId > 0) {
                        navController.navigate("${CookingRoutes.RECIPE_EDIT}?recipeId=$recipeId")
                    } else {
                        navController.navigate(CookingRoutes.RECIPE_EDIT)
                    }
                },
                onNavigateToCookMode = { recipeId ->
                    navController.navigate("${CookingRoutes.COOK_MODE}/$recipeId")
                },
                onNavigateToBackup = {
                    navController.navigate(CookingRoutes.BACKUP)
                },
                onQuickDraftClick = {
                    navController.navigate("${CookingRoutes.RECIPE_EDIT}?draft=true")
                },
                onImportClipboardClick = {
                    navController.navigate(CookingRoutes.RECIPE_EDIT)
                }
            )
        }

        composable(
            route = "${CookingRoutes.RECIPE_EDIT}?recipeId={recipeId}&draft={draft}",
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("draft") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: -1L
            val isDraft = backStackEntry.arguments?.getBoolean("draft") ?: false

            val editId = if (recipeId > 0) recipeId else null
            val viewModel: RecipeEditViewModel = viewModel(
                factory = RecipeEditViewModel.Factory(repository, editId)
            )

            if (isDraft && editId == null) {
                viewModel.setQuickDraftMode(true)
            }

            RecipeEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRecipeSaved = { savedId ->
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${CookingRoutes.COOK_MODE}/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            val viewModel: CookModeViewModel = viewModel(
                factory = CookModeViewModel.Factory(repository, recipeId)
            )

            CookModeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { editId ->
                    navController.navigate("${CookingRoutes.RECIPE_EDIT}?recipeId=$editId")
                }
            )
        }

        composable(CookingRoutes.BACKUP) {
            val viewModel: BackupViewModel = viewModel(
                factory = BackupViewModel.Factory(repository)
            )
            BackupScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

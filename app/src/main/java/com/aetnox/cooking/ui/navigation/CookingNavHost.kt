package com.aetnox.cooking.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aetnox.cooking.data.database.AppDatabase
import com.aetnox.cooking.data.remote.AppStartupManager
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.ui.screen.CookViewTab
import com.aetnox.cooking.ui.component.ForcedUpdateDialog
import com.aetnox.cooking.ui.screen.BackupScreen
import com.aetnox.cooking.ui.screen.CookModeScreen
import com.aetnox.cooking.ui.screen.RecipeEditScreen
import com.aetnox.cooking.ui.screen.RecipeListScreen
import com.aetnox.cooking.ui.util.AppLogger
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
    navController: NavHostController = rememberNavController(),
    initialRecipeId: Long? = null,
    initialStepIndex: Int? = null,
    onNavigatedToDeepLink: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val repository = RecipeRepository(database.recipeDao())

    val startupState by AppStartupManager.startupState.collectAsState()

    LaunchedEffect(Unit) {
        AppStartupManager.checkAppLaunch(context)
    }

    LaunchedEffect(initialRecipeId, initialStepIndex) {
        if (initialRecipeId != null && initialRecipeId > 0) {
            val stepParam = initialStepIndex ?: 0
            AppLogger.d("NAV", "Handling DeepLink intent to recipeId=$initialRecipeId, step=$stepParam")
            navController.navigate("${CookingRoutes.COOK_MODE}/$initialRecipeId?stepIndex=$stepParam&initialTab=STEP_BY_STEP")
            onNavigatedToDeepLink()
        }
    }

    if (startupState.isUpdateRequired) {
        ForcedUpdateDialog(
            updateUrl = startupState.updateUrl,
            minVersionCode = startupState.minVersionCode
        )
    }

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
                        AppLogger.d("NAV", "Navigating to Edit Screen for recipeId=$recipeId")
                        navController.navigate("${CookingRoutes.RECIPE_EDIT}?recipeId=$recipeId")
                    } else {
                        AppLogger.d("NAV", "Navigating to New Recipe Creation Screen")
                        navController.navigate(CookingRoutes.RECIPE_EDIT)
                    }
                },
                onNavigateToCookMode = { recipeId, initialTab ->
                    AppLogger.d("NAV", "Navigating to CookModeScreen for recipeId=$recipeId, initialTab=$initialTab")
                    navController.navigate("${CookingRoutes.COOK_MODE}/$recipeId?initialTab=$initialTab")
                },
                onNavigateToBackup = {
                    AppLogger.d("NAV", "Navigating to Backup & Settings Screen")
                    navController.navigate(CookingRoutes.BACKUP)
                },
                onQuickDraftClick = {
                    AppLogger.d("NAV", "Navigating to Quick Draft Creation")
                    navController.navigate("${CookingRoutes.RECIPE_EDIT}?draft=true")
                },
                onImportClipboardClick = {
                    AppLogger.d("NAV", "Navigating to Recipe Import from Clipboard")
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
                onNavigateBack = {
                    AppLogger.d("NAV", "Popping back stack from RecipeEditScreen")
                    navController.popBackStack()
                },
                onRecipeSaved = { savedId ->
                    AppLogger.d("NAV", "Recipe saved successfully (id=$savedId), popping back stack")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${CookingRoutes.COOK_MODE}/{recipeId}?stepIndex={stepIndex}&initialTab={initialTab}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType },
                navArgument("stepIndex") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("initialTab") {
                    type = NavType.StringType
                    defaultValue = "STEP_BY_STEP"
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            val stepIndexParam = backStackEntry.arguments?.getInt("stepIndex") ?: -1
            val initialTabParam = backStackEntry.arguments?.getString("initialTab") ?: "STEP_BY_STEP"

            val viewModel: CookModeViewModel = viewModel(
                factory = CookModeViewModel.Factory(repository, recipeId)
            )

            LaunchedEffect(stepIndexParam) {
                if (stepIndexParam >= 0) {
                    viewModel.setActiveStepIndex(stepIndexParam)
                }
            }

            CookModeScreen(
                viewModel = viewModel,
                initialTab = if (initialTabParam == "OVERVIEW") CookViewTab.OVERVIEW else CookViewTab.STEP_BY_STEP,
                onNavigateBack = {
                    AppLogger.d("NAV", "Popping back stack from CookModeScreen")
                    navController.popBackStack()
                },
                onNavigateToEdit = { editId ->
                    AppLogger.d("NAV", "Navigating to Edit Screen from CookModeScreen for recipeId=$editId")
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
                onNavigateBack = {
                    AppLogger.d("NAV", "Popping back stack from BackupScreen")
                    navController.popBackStack()
                }
            )
        }
    }
}

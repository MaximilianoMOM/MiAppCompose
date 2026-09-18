package com.sena.crud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sena.crud.ui.auth.AuthUiState
import com.sena.crud.ui.auth.AuthViewModel
import com.sena.crud.ui.auth.LoginScreen
import com.sena.crud.ui.auth.RegisterScreen
import com.sena.crud.ui.task.DraftsScreen
import com.sena.crud.ui.task.TaskFormScreen
import com.sena.crud.ui.task.TaskListScreen
import com.sena.crud.ui.task.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = hiltViewModel()
            val taskViewModel: TaskViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            // Monitorear el estado de la autenticación de forma global para redirigir reactivamente
            LaunchedEffect(authUiState) {
                when (authUiState) {
                    is AuthUiState.Authenticated -> {
                        // Al estar autenticado, navegamos de forma segura a la lista principal limpiando el backstack
                        navController.navigate("tasks") {
                            popUpTo("login") { inclusive = true }
                            popUpTo("register") { inclusive = true }
                        }
                    }
                    is AuthUiState.Idle, is AuthUiState.Error -> {
                        // Si el usuario no ha iniciado sesión o se desautentica, vuelve al Login
                        if (navController.currentDestination?.route != "login" && 
                            navController.currentDestination?.route != "register") {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    is AuthUiState.Loading -> {
                        // Dejamos que la pantalla interna maneje su cargador visual
                    }
                }
            }

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { navController.navigate("register") },
                        onLoginSuccess = { /* Manejado por LaunchedEffect global */ }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = { navController.navigate("login") },
                        onRegisterSuccess = { /* Manejado por LaunchedEffect global */ }
                    )
                }

                composable("tasks") {
                    val state = authUiState
                    if (state is AuthUiState.Authenticated) {
                        TaskListScreen(
                            taskViewModel = taskViewModel,
                            ownerId = state.user.id,
                            ownerName = state.user.displayName ?: state.user.email,
                            onNavigateToForm = { taskId -> navController.navigate("form?taskId=$taskId") },
                            onNavigateToDrafts = { navController.navigate("drafts") },
                            onLogout = { authViewModel.logout() }
                        )
                    }
                }

                composable(
                    route = "form?taskId={taskId}",
                    arguments = listOf(navArgument("taskId") { 
                        type = NavType.StringType
                        defaultValue = "" 
                    })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                    val state = authUiState
                    if (state is AuthUiState.Authenticated) {
                        TaskFormScreen(
                            taskViewModel = taskViewModel,
                            ownerId = state.user.id,
                            taskId = taskId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("drafts") {
                    DraftsScreen(
                        taskViewModel = taskViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

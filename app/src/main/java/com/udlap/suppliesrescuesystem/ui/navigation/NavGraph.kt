package com.udlap.suppliesrescuesystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.udlap.suppliesrescuesystem.ui.auth.LoginScreen
import com.udlap.suppliesrescuesystem.ui.auth.RegisterScreen
import androidx.compose.material3.Text

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{role}") {
        fun createRoute(role: String) = "home/$role"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(Screen.Home.createRoute(role)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    navController.navigate(Screen.Home.createRoute(role)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        composable(Screen.Home.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            Text(text = "Welcome $role! (Home Screen Placeholder)")
        }
    }
}

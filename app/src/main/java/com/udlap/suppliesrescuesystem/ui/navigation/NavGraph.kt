package com.udlap.suppliesrescuesystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.udlap.suppliesrescuesystem.ui.auth.LoginScreen
import com.udlap.suppliesrescuesystem.ui.auth.RegisterScreen
import com.udlap.suppliesrescuesystem.ui.donor.DonorHomeScreen
import com.udlap.suppliesrescuesystem.ui.donor.PublishBatchScreen
import com.udlap.suppliesrescuesystem.ui.volunteer.VolunteerHomeScreen
import com.udlap.suppliesrescuesystem.ui.recipient.RecipientHomeScreen
import androidx.compose.material3.Text

/**
 * Defines the navigation routes available in the application.
 *
 * Each object represents a distinct screen or navigation target.
 *
 * @property route The string identifier for the navigation destination.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object DonorHome : Screen("donor_home")
    object VolunteerHome : Screen("volunteer_home")
    object RecipientHome : Screen("recipient_home")
    object PublishBatch : Screen("publish_batch")
    object HomePlaceholder : Screen("home_placeholder/{role}") {
        fun createRoute(role: String) = "home_placeholder/$role"
    }
}

/**
 * The root navigation graph for the application.
 *
 * Manages the navigation between different screens using [NavHost].
 *
 * @param navController The controller used to perform navigation actions.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navigateByRole(navController, role)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    navigateByRole(navController, role)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        
        // Donor Flow
        composable(Screen.DonorHome.route) {
            DonorHomeScreen(
                onNavigateToPublish = {
                    navController.navigate(Screen.PublishBatch.route)
                }
            )
        }
        composable(Screen.PublishBatch.route) {
            PublishBatchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Volunteer Flow
        composable(Screen.VolunteerHome.route) {
            VolunteerHomeScreen()
        }

        // Recipient Flow
        composable(Screen.RecipientHome.route) {
            RecipientHomeScreen()
        }

        composable(Screen.HomePlaceholder.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            Text(text = "Welcome $role! (Home Screen Placeholder)")
        }
    }
}

/**
 * Navigates the user to the appropriate home screen based on their role.
 *
 * Clears the back stack to prevent the user from navigating back to the login screen.
 *
 * @param navController The [NavHostController] to use for navigation.
 * @param role The user's role (e.g., "DONOR", "VOLUNTEER", "RECIPIENT").
 */
private fun navigateByRole(navController: NavHostController, role: String) {
    val destination = when (role) {
        "DONOR" -> Screen.DonorHome.route
        "VOLUNTEER" -> Screen.VolunteerHome.route
        "RECIPIENT" -> Screen.RecipientHome.route
        else -> Screen.HomePlaceholder.createRoute(role)
    }
    navController.navigate(destination) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}

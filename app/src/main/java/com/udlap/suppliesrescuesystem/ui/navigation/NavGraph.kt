package com.udlap.suppliesrescuesystem.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.udlap.suppliesrescuesystem.ui.auth.LoginScreen
import com.udlap.suppliesrescuesystem.ui.auth.RegisterScreen
import com.udlap.suppliesrescuesystem.ui.auth.CompleteProfileScreen
import com.udlap.suppliesrescuesystem.ui.donor.DonorHomeScreen
import com.udlap.suppliesrescuesystem.ui.donor.PublishBatchScreen
import com.udlap.suppliesrescuesystem.ui.volunteer.VolunteerHomeScreen
import com.udlap.suppliesrescuesystem.ui.recipient.RecipientHomeScreen
import com.udlap.suppliesrescuesystem.ui.profile.ProfileScreen
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import com.udlap.suppliesrescuesystem.ui.auth.AuthState
import com.udlap.suppliesrescuesystem.ui.auth.AuthViewModel

/**
 * Defines the navigation routes available in the application.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object CompleteProfile : Screen("complete_profile")
    object DonorHome : Screen("donor_home")
    object VolunteerHome : Screen("volunteer_home")
    object RecipientHome : Screen("recipient_home")
    object PublishBatch : Screen("publish_batch")
    object Profile : Screen("profile")
    object HomePlaceholder : Screen("home_placeholder/{role}") {
        fun createRoute(role: String) = "home_placeholder/$role"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onAuthenticated = { role ->
                    navigateByRole(navController, role)
                },
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onIncompleteProfile = {
                    navController.navigate(Screen.CompleteProfile.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navigateByRole(navController, role)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onIncompleteProfile = {
                    navController.navigate(Screen.CompleteProfile.route)
                },
                viewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    navigateByRole(navController, role)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                onCompleteSuccess = { role ->
                    navigateByRole(navController, role)
                },
                viewModel = authViewModel
            )
        }
        
        // Donor Flow
        composable(Screen.DonorHome.route) {
            DonorHomeScreen(
                onNavigateToPublish = {
                    navController.navigate(Screen.PublishBatch.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
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
            VolunteerHomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Recipient Flow
        composable(Screen.RecipientHome.route) {
            RecipientHomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Shared Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.HomePlaceholder.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            Text(text = "Welcome $role! (Home Screen Placeholder)")
        }
    }
}

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onAuthenticated: (String) -> Unit,
    onNotAuthenticated: () -> Unit,
    onIncompleteProfile: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onAuthenticated((authState as AuthState.Success).user.role)
            is AuthState.NoSession -> onNotAuthenticated()
            is AuthState.IncompleteProfile -> onIncompleteProfile()
            else -> { /* Keep loading */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF4CAF50))
    }
}

private fun navigateByRole(navController: NavHostController, role: String) {
    val destination = when (role) {
        "DONOR" -> Screen.DonorHome.route
        "VOLUNTEER" -> Screen.VolunteerHome.route
        "RECIPIENT" -> Screen.RecipientHome.route
        else -> Screen.HomePlaceholder.createRoute(role)
    }
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }
}

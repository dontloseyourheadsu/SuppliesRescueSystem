package com.udlap.suppliesrescuesystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.udlap.suppliesrescuesystem.ui.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity of the application, serving as the entry point for the UI.
 *
 * This activity is annotated with [AndroidEntryPoint] to enable dependency injection with Hilt.
 * It sets up the Compose content and initializes the navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

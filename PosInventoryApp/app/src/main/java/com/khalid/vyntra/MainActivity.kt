package com.khalid.vyntra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.khalid.vyntra.data.preferences.PreferencesManager
import com.khalid.vyntra.presentation.navigation.BottomNavBar
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.navigation.VyntraNavGraph
import com.khalid.vyntra.presentation.onboarding.OnboardingScreen
import com.khalid.vyntra.presentation.theme.VyntraTheme
import com.khalid.vyntra.worker.WorkerScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WorkerScheduler.scheduleLowStockCheck(this)

        setContent {
            val isDarkTheme by preferencesManager.isDarkTheme.collectAsState(initial = false)

            VyntraTheme(darkTheme = isDarkTheme) {
                // First-launch gate. While DataStore loads the flag (a few ms)
                // we render nothing — better than flashing the onboarding to a
                // returning user. Settings → About → "Replay welcome tour"
                // simply writes `false` back, which re-fires this gate.
                val hasSeen by preferencesManager.hasSeenOnboarding
                    .collectAsState(initial = null)
                when (hasSeen) {
                    null -> Unit
                    false -> OnboardingScreen(
                        onFinished = { /* DataStore flow drives the transition */ }
                    )
                    true -> MainAppShell(preferencesManager)
                }
            }
        }
    }
}

@Composable
private fun MainAppShell(@Suppress("UNUSED_PARAMETER") prefs: PreferencesManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Inventory.route,
        Screen.NewSale.route,
        Screen.Reports.route,
        Screen.Settings.route
    )
    // currentRoute is null for the first frame on a cold launch (back-stack
    // entry hasn't resolved yet). Treat that as "on the start destination"
    // so the bottom bar doesn't flicker out.
    val showBottomBar = currentRoute == null || currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        VyntraNavGraph(
            navController = navController,
            // consumeWindowInsets tells per-screen TopAppBars not to add the
            // status-bar inset again on top of what this Scaffold already
            // gave us in innerPadding.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

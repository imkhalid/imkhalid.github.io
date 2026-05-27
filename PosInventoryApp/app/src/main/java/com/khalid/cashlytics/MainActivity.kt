package com.khalid.cashlytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.khalid.cashlytics.data.preferences.PreferencesManager
import com.khalid.cashlytics.presentation.navigation.BottomNavBar
import com.khalid.cashlytics.presentation.navigation.CashlyticsNavGraph
import com.khalid.cashlytics.presentation.navigation.Screen
import com.khalid.cashlytics.presentation.theme.CashlyticsTheme
import com.khalid.cashlytics.worker.WorkerScheduler
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

            CashlyticsTheme(darkTheme = isDarkTheme) {
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

                Scaffold(
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    CashlyticsNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

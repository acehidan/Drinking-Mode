package dev.ace.applock

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.rememberNavController
import dev.ace.applock.core.navigation.AppNavHost
import dev.ace.applock.core.navigation.NavigationManager
import dev.ace.applock.core.navigation.Screen
import dev.ace.applock.core.utils.setAppLocale
import dev.ace.applock.ui.theme.AppLockTheme

class MainActivity : FragmentActivity() {

    private lateinit var navigationManager: NavigationManager

    override fun attachBaseContext(newBase: Context) {
        val app = newBase.applicationContext as AppLockApplication
        val language = app.preferencesRepository.getLanguage()
        super.attachBaseContext(newBase.setAppLocale(language))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigationManager = NavigationManager(this)

        setContent {
            AppLockTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()
                    val startDestination = navigationManager.determineStartDestination()

                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        handleOnResume(navController)
                    }
                }
            }
        }
    }

    private fun handleOnResume(navController: androidx.navigation.NavHostController) {
        val currentRoute = navController.currentDestination?.route

        if (navigationManager.shouldSkipPasswordCheck(currentRoute)) {
            return
        }

        if (currentRoute != Screen.PasswordOverlay.route) {
            navController.navigate(Screen.PasswordOverlay.route)
        }
    }
}

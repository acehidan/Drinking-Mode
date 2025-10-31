package dev.ace.applock.core.navigation

sealed class Screen(val route: String) {
    data object AppIntro : Screen("app_intro")
    data object SetPassword : Screen("set_password")
    data object ChangePassword : Screen("change_password")
    data object Home : Screen("home")
    data object Main : Screen("main")
    data object PasswordOverlay : Screen("password_overlay")
    data object Settings : Screen("settings")
    data object ContactSupport : Screen("contact_support")
    data object Language : Screen("language")
    data object TriggerExclusions : Screen("trigger_exclusions")
    data object TestDifficulty : Screen("test_difficulty")
}

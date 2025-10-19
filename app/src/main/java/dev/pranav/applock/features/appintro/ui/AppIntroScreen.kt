package dev.pranav.applock.features.appintro.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.NavController
import dev.pranav.appintro.AppIntro
import dev.pranav.appintro.IntroPage
import dev.pranav.applock.R
import dev.pranav.applock.core.navigation.Screen
import dev.pranav.applock.core.utils.appLockRepository
import dev.pranav.applock.core.utils.hasUsagePermission
import dev.pranav.applock.core.utils.isAccessibilityServiceEnabled
import dev.pranav.applock.core.utils.isMiui
import dev.pranav.applock.core.utils.launchBatterySettings
import dev.pranav.applock.core.utils.launchMiuiPermissionsEditor
import dev.pranav.applock.data.repository.BackendImplementation
import dev.pranav.applock.features.appintro.domain.AppIntroManager
import dev.pranav.applock.services.ExperimentalAppLockService
import dev.pranav.applock.services.ShizukuAppLockService
import dev.pranav.applock.ui.icons.Accessibility
import dev.pranav.applock.ui.icons.BatterySaver
import dev.pranav.applock.ui.icons.Display
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

@Composable
fun AppIntroScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // ... (other states remain the same)
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var notificationPermissionGranted by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    val requestPermissionLauncher =
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                notificationPermissionGranted = isGranted
            }
        } else null

    val onFinishCallback = {
        AppIntroManager.markIntroAsCompleted(context)
        navController.navigate(Screen.SetPassword.route) {
            popUpTo(Screen.AppIntro.route) { inclusive = true }
        }
    }

    val pages = mutableListOf<IntroPage>()

    pages.add(
        IntroPage(
            title = stringResource(R.string.welc_applock),
            description = stringResource(R.string.welcome_desc),
            icon = Icons.Filled.Lock,
            backgroundColor = Color(0xFF00A86B),
            contentColor = Color.White
        )
    )

    pages.add(
        IntroPage(
            title = stringResource(R.string.display_over_apps),
            description = stringResource(R.string.display_over_apps_desc),
            icon = Display,
            backgroundColor = Color(0xFF00A86B),
            contentColor = Color.White,
            onNext = {
                overlayPermissionGranted = Settings.canDrawOverlays(context)
                if (!overlayPermissionGranted) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                    Toast.makeText(context, "Please grant the permission to continue", Toast.LENGTH_SHORT).show()
                    false // prevent advancing
                } else {
                    true // permission granted, allow advancing
                }
            }
        )
    )

    // Add MIUI specific permission page if on a MIUI device
    if (isMiui()) {
        pages.add(
            IntroPage(
                title = "Xiaomi/MIUI Permissions",
                description = "For this app to work correctly on your device, please grant the 'Display pop-up windows' permission.",
                icon = Icons.Filled.Security,
                backgroundColor = Color(0xFF00A86B),
                contentColor = Color.White,
                onNext = {
                    launchMiuiPermissionsEditor(context)
                    true // We allow to proceed, user needs to grant it manually.
                }
            )
        )
    }

    pages.add(
        IntroPage(
            title = stringResource(R.string.disable_battery_optimization_title),
            description = stringResource(R.string.disable_battery_optimization_desc),
            icon = BatterySaver,
            backgroundColor = Color(0xFF00A86B),
            contentColor = Color.White,
            onNext = {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    launchBatterySettings(context)
                }
                true // Allow proceeding even if user doesn't grant it now.
            }
        )
    )

    pages.add(
        IntroPage(
            title = stringResource(R.string.notif_perm),
            description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) stringResource(R.string.notif_perm_desc) else stringResource(R.string.notif_perm_granted),
            icon = Icons.Default.Notifications,
            backgroundColor = Color(0xFF00A86B),
            contentColor = Color.White,
            onNext = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!notificationPermissionGranted) {
                        requestPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        return@IntroPage false
                    }
                }
                true
            }
        )
    )

    pages.add(
        IntroPage(
            title = "Choose Your Method",
            description = "Select the method this app will use to detect opened apps.",
            icon = Icons.Default.QueryStats,
            backgroundColor = Color(0xFF00A86B),
            contentColor = Color.White,
            customContent = { /* Method Selection UI Here */ }
        )
    )

    pages.add(
        IntroPage(
            title = stringResource(R.string.lets_build_safe_zone),
            description = stringResource(R.string.restrict_apps_description),
            backgroundColor = Color(0xFFF5F5DC),
            contentColor = Color.Black,
            customContent = { AppSelectionPage() }
        )
    )

    AppIntro(
        pages = pages,
        onFinish = onFinishCallback,
        showSkipButton = false
    )
}

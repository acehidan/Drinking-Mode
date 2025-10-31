package dev.ace.applock.features.appintro.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import androidx.navigation.NavController
import dev.pranav.appintro.AppIntro
import dev.pranav.appintro.IntroPage
import dev.ace.applock.R
import dev.ace.applock.core.navigation.Screen
import dev.ace.applock.core.utils.*
import dev.ace.applock.data.repository.BackendImplementation
import dev.ace.applock.features.appintro.domain.AppIntroManager
import dev.ace.applock.services.ExperimentalAppLockService
import dev.ace.applock.services.ShizukuAppLockService
import dev.ace.applock.ui.icons.Accessibility
import dev.ace.applock.ui.icons.BatterySaver
import dev.ace.applock.ui.icons.Display
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

enum class AppUsageMethod {
    ACCESSIBILITY,
    USAGE_STATS,
    SHIZUKU
}

@Composable
fun MethodSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(
                alpha = 0.1f
            )
        ),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun AppIntroScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var selectedMethod by remember { mutableStateOf(AppUsageMethod.ACCESSIBILITY) }
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var notificationPermissionGranted by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var usageStatsPermissionGranted by remember { mutableStateOf(context.hasUsagePermission()) }
    var accessibilityServiceEnabled by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }

    val requestPermissionLauncher =
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    notificationPermissionGranted = true
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.notification_permission_required_desc),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else null

    val shizukuPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(
                    context,
                    context.getString(R.string.shizuku_permission_granted),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.shizuku_permission_required_desc),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    LaunchedEffect(key1 = context) {
        overlayPermissionGranted = Settings.canDrawOverlays(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted =
                NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        accessibilityServiceEnabled = context.isAccessibilityServiceEnabled()
    }

    val onFinishCallback = {
        AppIntroManager.markIntroAsCompleted(context)
        navController.navigate(Screen.SetPassword.route) {
            popUpTo(Screen.AppIntro.route) { inclusive = true }
        }
    }

    val pages = remember {
        mutableListOf<IntroPage>().apply {
            add(
                IntroPage(
                    title = context.getString(R.string.welc_applock),
                    description = context.getString(R.string.welcome_desc),
                    icon = Icons.Filled.Lock,
                    backgroundColor = Color(0xFFFFFBE8),
                    contentColor = Color(0xFF01A87B)
                )
            )
            add(
                IntroPage(
                    title = context.getString(R.string.display_over_apps),
                    description = context.getString(R.string.display_over_apps_desc),
                    icon = Display,
                    backgroundColor = Color(0xFFFFFBE8),
                    contentColor = Color(0xFF01A87B),
                    onNext = {
                        overlayPermissionGranted = Settings.canDrawOverlays(context)
                        if (!overlayPermissionGranted) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.data = "package:${context.packageName}".toUri()
                            Toast.makeText(
                                context,
                                context.getString(R.string.allow_display_over_other_apps),
                                Toast.LENGTH_LONG
                            ).show()
                            context.startActivity(intent)
                            false
                        } else {
                            true
                        }
                    }
                )
            )
            if (isMiui()) {
                add(
                    IntroPage(
                        title = "Xiaomi/MIUI Permissions",
                        description = "For this app to work correctly on your device, please grant the 'Display pop-up windows' permission.",
                        icon = Icons.Filled.Security,
                        backgroundColor = Color(0xFFFFFBE8),
                        contentColor = Color(0xFF01A87B),
                        onNext = {
                            launchMiuiPermissionsEditor(context)
                            true // We allow to proceed, user needs to grant it manually.
                        }
                    )
                )
            }
            add(
                IntroPage(
                    title = context.getString(R.string.disable_battery_optimization_title),
                    description = context.getString(R.string.disable_battery_optimization_desc),
                    icon = BatterySaver,
                    backgroundColor = Color(0xFFFFFBE8),
                    contentColor = Color(0xFF01A87B),
                    onNext = {
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val isIgnoringOptimizations =
                            powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        if (!isIgnoringOptimizations) {
                            launchBatterySettings(context)
                            return@IntroPage false
                        }
                        return@IntroPage true
                    }
                )
            )
            add(
                IntroPage(
                    title = context.getString(R.string.notif_perm),
                    description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        context.getString(R.string.notif_perm_desc)
                    else context.getString(R.string.notif_perm_granted),
                    icon = Icons.Default.Notifications,
                    backgroundColor = Color(0xFFFFFBE8),
                    contentColor = Color(0xFF01A87B),
                    onNext = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val isGrantedCurrently =
                                NotificationManagerCompat.from(context).areNotificationsEnabled()
                            notificationPermissionGranted = isGrantedCurrently
                            if (!isGrantedCurrently) {
                                requestPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                return@IntroPage false
                            }
                        }
                        true
                    }
                )
            )
        }
    }

    val methodSpecificPages = when (selectedMethod) {
        AppUsageMethod.ACCESSIBILITY -> listOf(
            IntroPage(
                title = stringResource(R.string.accessibility_service_title),
                description = stringResource(R.string.app_intro_accessibility_desc),
                icon = Accessibility,
                backgroundColor = Color(0xFFFFFBE8),
                contentColor = Color(0xFF01A87B),
                onNext = {
                    accessibilityServiceEnabled = context.isAccessibilityServiceEnabled()
                    if (!accessibilityServiceEnabled) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        false
                    } else {
                        context.appLockRepository()
                            .setBackendImplementation(BackendImplementation.ACCESSIBILITY)
                        true
                    }
                }
            )
        )
        AppUsageMethod.USAGE_STATS -> listOf(
            IntroPage(
                title = stringResource(R.string.app_intro_usage_stats_title),
                description = stringResource(R.string.app_intro_usage_stats_desc),
                icon = Icons.Default.QueryStats,
                backgroundColor = Color(0xFFFFFBE8),
                contentColor = Color(0xFF01A87B),
                onNext = {
                    usageStatsPermissionGranted = context.hasUsagePermission()
                    if (!usageStatsPermissionGranted) {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        false
                    } else {
                        context.appLockRepository()
                            .setBackendImplementation(BackendImplementation.USAGE_STATS)
                        context.startService(
                            Intent(context, ExperimentalAppLockService::class.java)
                        )
                        true
                    }
                }
            )
        )
        AppUsageMethod.SHIZUKU -> listOf(
            IntroPage(
                title = stringResource(R.string.shizuku_service_title),
                description = stringResource(R.string.app_intro_shizuku_desc),
                icon = Icons.Default.QueryStats,
                backgroundColor = Color(0xFFFFFBE8),
                contentColor = Color(0xFF01A87B),
                onNext = {
                    val isGranted = if (Shizuku.isPreV11()) {
                        checkSelfPermission(
                            context,
                            ShizukuProvider.PERMISSION
                        ) == PermissionChecker.PERMISSION_GRANTED
                    } else {
                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                    }

                    if (!isGranted) {
                        if (Shizuku.isPreV11()) {
                            shizukuPermissionLauncher.launch(ShizukuProvider.PERMISSION)
                        } else {
                            Shizuku.requestPermission(423)
                        }
                        false
                    } else {
                        context.appLockRepository()
                            .setBackendImplementation(BackendImplementation.SHIZUKU)
                        context.startService(
                            Intent(context, ShizukuAppLockService::class.java)
                        )
                        true
                    }
                }
            )
        )
    }

    val appSelectionPage = IntroPage(
        title = stringResource(R.string.lets_build_safe_zone),
        description = stringResource(R.string.restrict_apps_description),
        icon = Icons.Filled.Lock,
        backgroundColor = Color(0xFFFFFBE8),
        contentColor = Color(0xFF388E3C),
        customContent = {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp)) {
                AppSelectionPage()
            }
        },
        onNext = { true }
    )

    val finalPage = IntroPage(
        title = stringResource(R.string.app_intro_complete_privacy_title),
        description = stringResource(R.string.app_intro_complete_privacy_desc),
        icon = Icons.Default.Lock,
        backgroundColor = Color(0xFFFFFBE8),
        contentColor = Color(0xFF01A87B),
        onNext = {
            overlayPermissionGranted = Settings.canDrawOverlays(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionGranted =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

            val methodPermissionGranted = when (selectedMethod) {
                AppUsageMethod.ACCESSIBILITY -> context.isAccessibilityServiceEnabled()
                AppUsageMethod.USAGE_STATS -> context.hasUsagePermission()
                AppUsageMethod.SHIZUKU -> {
                    if (Shizuku.isPreV11()) {
                        PermissionChecker.checkSelfPermission(
                            context,
                            ShizukuProvider.PERMISSION
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                    }
                }
            }

            val allPermissionsGranted = overlayPermissionGranted && notificationPermissionGranted && methodPermissionGranted

            if (!allPermissionsGranted) {
                Toast.makeText(
                    context,
                    context.getString(R.string.all_permissions_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
            allPermissionsGranted
        }
    )

    val allPages = pages + methodSpecificPages + appSelectionPage + finalPage

    AppIntro(
        pages = allPages,
        onFinish = onFinishCallback,
        showSkipButton = false
    )
}

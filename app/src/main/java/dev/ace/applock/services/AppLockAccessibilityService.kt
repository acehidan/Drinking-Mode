package dev.ace.applock.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import dev.ace.applock.core.broadcast.DeviceAdmin
import dev.ace.applock.core.utils.appLockRepository
import dev.ace.applock.core.utils.enableAccessibilityServiceWithShizuku
import dev.ace.applock.data.repository.AppLockRepository
import dev.ace.applock.data.repository.BackendImplementation
import dev.ace.applock.features.lockscreen.ui.PasswordOverlayActivity
import dev.ace.applock.services.AppLockConstants.ACCESSIBILITY_SETTINGS_CLASSES
import dev.ace.applock.services.AppLockConstants.ADMIN_CONFIG_CLASSES
import dev.ace.applock.services.AppLockConstants.EXCLUDED_APPS
import dev.ace.applock.services.AppLockConstants.KNOWN_RECENTS_CLASSES
import rikka.shizuku.Shizuku

@SuppressLint("AccessibilityPolicy")
class AppLockAccessibilityService : AccessibilityService() {
    private val appLockRepository: AppLockRepository by lazy { applicationContext.appLockRepository() }
    private val keyboardPackages: List<String> by lazy { getKeyboardPackageNames() }

    private var recentsOpen = false

    private var lastForegroundPackage = ""

    enum class BiometricState {
        IDLE, AUTH_STARTED
    }

    companion object {
        private const val TAG = "AppLockAccessibility"
        private const val DEVICE_ADMIN_SETTINGS_PACKAGE = "com.android.settings"
        private const val APP_PACKAGE_PREFIX = "dev.pranav.applock"

        @Volatile
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        AppLockManager.currentBiometricState = BiometricState.IDLE
        startPrimaryBackendService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            packageNames = null
        }

        Log.d(TAG, "Accessibility service connected")
        AppLockManager.resetRestartAttempts(TAG)
        appLockRepository.setActiveBackend(BackendImplementation.ACCESSIBILITY)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "EVENT RECEIVED: Package=${event.packageName}, Class=${event.className}, Type=${AccessibilityEvent.eventTypeToString(event.eventType)}")

        if (appLockRepository.isAntiUninstallEnabled() && event.packageName == DEVICE_ADMIN_SETTINGS_PACKAGE) {
            checkForDeviceAdminDeactivation(event)
        }

        if (!appLockRepository.isProtectEnabled() || !isServiceRunning) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            when {
                (event.packageName == getSystemDefaultLauncherPackageName() && event.contentChangeTypes == AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_APPEARED) || (event.className == "com.android.launcher3.uioverrides.QuickstepLauncher" && event.text.toString()
                    .lowercase().contains("recent apps")) -> {
                    Log.d(TAG, "Entering recents")
                    recentsOpen = true
                    return
                }

                (event.packageName != getSystemDefaultLauncherPackageName() && recentsOpen) -> {
                    recentsOpen = false

                    // when user switches apps with recents

                    if (event.packageName != AppLockManager.temporarilyUnlockedApp && event.packageName !in appLockRepository.getTriggerExcludedApps()) {
                        AppLockManager.clearTemporarilyUnlockedApp()
                    }
                }

                (event.contentChangeTypes == AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED && event.packageName == getSystemDefaultLauncherPackageName()) -> {
                    // when user presses home, ie goes to home screen from recents
                    AppLockManager.clearTemporarilyUnlockedApp()
                    recentsOpen = false
                }

                (event.packageName == getSystemDefaultLauncherPackageName() && event.className == "com.android.launcher3.uioverrides.QuickstepLauncher") && event.text.toString()
                    .lowercase().contains("home screen") -> {
                    // when user presses home, ie goes to home screen from recents
                    AppLockManager.clearTemporarilyUnlockedApp()
                    recentsOpen = false
                }
            }
        }

        if (recentsOpen) {
            Log.d(TAG, "Recents opened, ignoring accessibility event $event")
        } else {
            Log.d(TAG, event.toString())
        }

        val packageName = event.packageName?.toString() ?: return

        if (applicationContext.isDeviceLocked()) {
            AppLockManager.appUnlockTimes.clear()
            AppLockManager.clearTemporarilyUnlockedApp()
            return
        }

        if (!shouldAccessibilityHandleLocking()) return

        if (packageName == APP_PACKAGE_PREFIX || packageName in keyboardPackages || packageName in EXCLUDED_APPS) return
        if (event.className in KNOWN_RECENTS_CLASSES) return

        val currentForegroundPackage = packageName
        val triggeringPackage = lastForegroundPackage
        lastForegroundPackage = currentForegroundPackage

        if (triggeringPackage in appLockRepository.getTriggerExcludedApps()) return

        checkAndLockApp(currentForegroundPackage, triggeringPackage, event.eventTime)
    }

    private fun shouldAccessibilityHandleLocking(): Boolean {
        return when (appLockRepository.getBackendImplementation()) {
            BackendImplementation.ACCESSIBILITY -> true
            BackendImplementation.SHIZUKU -> !applicationContext.isServiceRunning(
                ShizukuAppLockService::class.java
            )

            BackendImplementation.USAGE_STATS -> !applicationContext.isServiceRunning(
                ExperimentalAppLockService::class.java
            )
        }
    }


    private fun checkAndLockApp(packageName: String, triggeringPackage: String, currentTime: Long) {
        if (AppLockManager.isLockScreenShown.get() || AppLockManager.currentBiometricState == BiometricState.AUTH_STARTED) return
        Log.d(TAG, "CHECKING APP: '$packageName'")
        Log.d(TAG, "Currently locked apps: ${appLockRepository.getLockedApps()}")
        if (packageName !in appLockRepository.getLockedApps()) {
            Log.w(TAG, "REASON TO NOT LOCK: App is not in the locked apps list.")
            return
        }

        if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
            Log.w(TAG, "REASON TO NOT LOCK: App is temporarily unlocked.")
            return
        }

        val unlockDurationMinutes = appLockRepository.getUnlockTimeDuration()
        val unlockTimestamp = AppLockManager.appUnlockTimes[packageName] ?: 0L

        if (unlockDurationMinutes > 0 && unlockTimestamp > 0) {
            val durationMillis = unlockDurationMinutes * 60 * 1000L
            if (currentTime - unlockTimestamp < durationMillis) {
                Log.w(TAG, "REASON TO NOT LOCK: App is within the re-lock time limit.")
                return
            }

            AppLockManager.appUnlockTimes.remove(packageName)
        }

        Log.d(TAG, "Locked app detected: $packageName. Showing overlay.")
        AppLockManager.isLockScreenShown.set(true)
        AppLockManager.clearTemporarilyUnlockedApp()

        val intent = Intent(this, PasswordOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_FROM_BACKGROUND or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("locked_package", packageName)
            putExtra("triggering_package", triggeringPackage)
        }

        // Use a handler to slightly delay the lock screen launch to avoid race conditions on some devices like MIUI
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.i(TAG, "ALL CONDITIONS MET. Attempting to start Lock Screen for '$packageName'")
                startActivity(intent)
                Log.i(TAG, "startActivity() called successfully without crashing for '$packageName'")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start password overlay for '$packageName'", e) // Full stack trace
                AppLockManager.isLockScreenShown.set(false)
            }
        }, 50) // 50ms delay
    }

    private fun checkForDeviceAdminDeactivation(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        Log.d(TAG, "Checking for device admin deactivation for event: $event")


        if ((event.className == "com.android.settings.SubSettings" && event.text.any { it.contains("App Lock") }) || (event.className == "android.app.AlertDialog" && event.text.first()
                .contains("App Lock")) || (event.className in ACCESSIBILITY_SETTINGS_CLASSES && event.text.any {
                it.contains(
                    "App Lock"
                )
            })
        ) {
            Log.d(TAG, "Blocking accessibility service deactivation")
            // there ain't no way someone bypasses this without root/shizuku lol

            performGlobalAction(GLOBAL_ACTION_BACK)
            performGlobalAction(GLOBAL_ACTION_HOME)
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }

        val isDeviceAdminPage =
            (event.contentDescription?.contains("Device admin app") == true && event.className == "android.widget.FrameLayout") || (event.className in ADMIN_CONFIG_CLASSES)

        val isOurAppVisible = findNodeWithTextContaining(rootNode, "App Lock") != null ||
                findNodeWithTextContaining(rootNode, "AppLock") != null

        if (!isDeviceAdminPage || !isOurAppVisible) return

        try {
            val dpm: DevicePolicyManager? = getSystemService()
            val component = ComponentName(this, DeviceAdmin::class.java)

            if (dpm?.isAdminActive(component) == true) {
                // If the user is on the deactivation page and DPM is still active, block it.
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)
                Thread.sleep(100)
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                Toast.makeText(
                    this,
                    "Disable anti-uninstall from AppLock settings to remove this restriction.",
                    Toast.LENGTH_LONG
                ).show()
                Log.w(TAG, "Blocked device admin deactivation attempt.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/blocking device admin deactivation", e)
        }
    }

    private fun findNodeWithTextContaining(
        node: AccessibilityNodeInfo, text: String
    ): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) return node

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeWithTextContaining(child, text)
            if (result != null) return result
        }
        return null
    }

    private fun getKeyboardPackageNames(): List<String> =
        getSystemService<InputMethodManager>()?.enabledInputMethodList?.map { it.packageName }
            ?: emptyList()

    fun getSystemDefaultLauncherPackageName(): String {
        val packageManager = packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        // Query for all activities that can handle the HOME intent
        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        // A system launcher typically has the SYSTEM flag set, or is the one provided by the system.
        // We look for the system-provided one that resolves the intent.
        val systemLauncher = resolveInfoList.find { resolveInfo ->
            // We check if the package name is NOT an external app (i.e., not a third-party launcher)
            // by looking for the SYSTEM flag, or just filtering out known common third-party names
            // or prioritizing the one with the highest priority (though unreliable).

            // The most reliable check is looking for the system app flag.
            val isSystemApp =
                (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            // Exclude the currently running app package (if this code is running in a service/app)
            val isOurApp = resolveInfo.activityInfo.packageName == packageName

            return@find isSystemApp && !isOurApp
        }

        val systemLauncherPackage = systemLauncher?.activityInfo?.packageName ?: ""

        if (systemLauncherPackage.isEmpty()) {
            Log.w("LauncherHelper", "Could not find a clear system launcher package name.")
        }
        return systemLauncherPackage
    }

    private fun startPrimaryBackendService() {
        AppLockManager.stopAllOtherServices(this, AppLockAccessibilityService::class.java)

        when (appLockRepository.getBackendImplementation()) {
            BackendImplementation.SHIZUKU -> {
                Log.d(TAG, "Starting Shizuku service as primary backend")
                startService(Intent(this, ShizukuAppLockService::class.java))
            }

            BackendImplementation.USAGE_STATS -> {
                Log.d(TAG, "Starting Experimental service as primary backend")
                startService(Intent(this, ExperimentalAppLockService::class.java))
            }

            else -> {
                Log.d(TAG, "Accessibility service is the primary backend.")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Accessibility service unbound")
        isServiceRunning = false
        AppLockManager.startFallbackServices(this, AppLockAccessibilityService::class.java)

        if (Shizuku.pingBinder() && appLockRepository.isAntiUninstallEnabled()) {
            enableAccessibilityServiceWithShizuku(ComponentName(packageName, javaClass.name))
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.d(TAG, "Accessibility service destroyed")
        AppLockManager.startFallbackServices(this, AppLockAccessibilityService::class.java)
    }
}

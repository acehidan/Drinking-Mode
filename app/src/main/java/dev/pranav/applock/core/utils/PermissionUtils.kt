package dev.pranav.applock.core.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

fun isMiui(): Boolean {
    return Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)
}

fun launchMiuiPermissionsEditor(context: Context) {
    try {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
        intent.putExtra("extra_pkgname", context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        Toast.makeText(context, "Find 'Display pop-up windows' and grant the permission.", Toast.LENGTH_LONG).show()
    } catch (e: ActivityNotFoundException) {
        // Fallback for older MIUI versions or if the activity is not found
        try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
            intent.putExtra("extra_pkgname", context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Find 'Display pop-up windows' and grant the permission.", Toast.LENGTH_LONG).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open MIUI permission settings automatically. Please do it manually from the Security app.", Toast.LENGTH_LONG).show()
        }
    }
}

package dev.ace.applock.core.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.Locale

fun Context.setAppLocale(language: String): ContextWrapper {
    val locale = when (language) {
        "Myanmar" -> Locale("my")
        "Korean" -> Locale("ko")
        else -> Locale.ENGLISH
    }
    Locale.setDefault(locale)

    val config = this.resources.configuration
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
    } else {
        @Suppress("DEPRECATION")
        config.locale = locale
    }
    @Suppress("DEPRECATION")
    this.resources.updateConfiguration(config, this.resources.displayMetrics)

    return ContextWrapper(this)
}

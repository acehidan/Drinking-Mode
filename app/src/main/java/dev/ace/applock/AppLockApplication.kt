package dev.ace.applock

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import dev.ace.applock.data.repository.AppLockRepository
import dev.ace.applock.data.repository.PreferencesRepository
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.sui.Sui

class AppLockApplication : Application() {

    lateinit var appLockRepository: AppLockRepository
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initializeHiddenApiBypass()
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
    }

    private fun initializeHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("L")
                Log.d(TAG, "Hidden API bypass initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize hidden API bypass", e)
            }
        }
    }

    private fun initializeComponents() {
        try {
            appLockRepository = AppLockRepository(this)
            preferencesRepository = PreferencesRepository(this)
            initializeSui()
            Log.d(TAG, "Application components initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize application components", e)
        }
    }

    private fun initializeSui() {
        try {
            Sui.init(packageName)
            Log.d(TAG, "Sui initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Sui", e)
        }
    }

    companion object {
        private const val TAG = "AppLockApplication"
    }
}

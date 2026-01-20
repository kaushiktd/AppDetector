package com.appdetector

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.text.TextUtils

class AppDetectorModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private var reactContextStatic: ReactApplicationContext? = null
        private var lastPackage: String? = null

        fun sendAppChangeEvent(packageName: String) {
            lastPackage = packageName
            reactContextStatic
                ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onAppChanged", packageName)
        }

        fun getLastPackage(): String? {
            return lastPackage
        }
    }

    init {
        reactContextStatic = reactContext
    }

    override fun getName(): String = "AppDetector"

    @ReactMethod
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        reactContext.startActivity(intent)
    }

    // Inside AppDetectorModule class
    @ReactMethod
    fun isAccessibilityServiceEnabled(promise: Promise) {
        try {
            val enabledServices = Settings.Secure.getString(
                reactContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val service = ComponentName(
                reactContext.packageName,
                "com.appdetector.AppDetectorAccessibilityService"
            ).flattenToString()

            val isEnabled = !TextUtils.isEmpty(enabledServices) && enabledServices.contains(service)
            promise.resolve(isEnabled)
        } catch (e: Exception) {
            promise.resolve(false)
        }
    }

    @ReactMethod
    fun openOverlayPermission() {
        if (!Settings.canDrawOverlays(reactContext)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${reactContext.packageName}")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            reactContext.startActivity(intent)
        }
    }

    @ReactMethod
    fun hasOverlayPermission(promise: Promise) {
        promise.resolve(Settings.canDrawOverlays(reactContext))
    }

    // 🔹 Toggle overlay via broadcast to Service
    @ReactMethod
    fun toggleOverlay(enable: Boolean) {
        val intent = Intent(reactContext, AppDetectorAccessibilityService::class.java)
        intent.action = if (enable) "SHOW_OVERLAY" else "HIDE_OVERLAY"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        reactContext.startService(intent)
    }

    @ReactMethod
    fun getLastDetectedApp(promise: Promise) {
        promise.resolve(lastPackage ?: "")
    }
}

package com.appdetector

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppDetectorAccessibilityService : AccessibilityService() {

    private var overlayVisible = true

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }

        OverlayManager.show(this, AppDetectorModule.getLastPackage() ?: "Detecting...")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        // if (pkg == "com.appdetector") return

        AppDetectorModule.sendAppChangeEvent(pkg)

        if (overlayVisible) {
            OverlayManager.updateText(pkg)
        }
    }

    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                "SHOW_OVERLAY" -> toggleOverlay(true)
                "HIDE_OVERLAY" -> toggleOverlay(false)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun toggleOverlay(show: Boolean) {
        overlayVisible = show
        if (show) {
            OverlayManager.show(this, AppDetectorModule.getLastPackage() ?: "Detecting...")
        } else {
            OverlayManager.hide()
        }
    }
}

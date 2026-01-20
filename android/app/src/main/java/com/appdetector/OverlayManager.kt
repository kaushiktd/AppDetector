package com.appdetector

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

object OverlayManager {

    private var overlayView: View? = null
    private var textView: TextView? = null
    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null
    private var isAdded = false

    fun show(context: Context, initialText: String = "Detecting...") {
        val appContext = context.applicationContext
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (overlayView == null) {
            overlayView = LayoutInflater.from(appContext).inflate(R.layout.overlay_view, null)
            textView = overlayView!!.findViewById(R.id.overlayText)
            textView?.text = initialText
        }

        if (params == null) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 40
                y = 100
            }
        }

        try {
            if (overlayView?.parent != null) {
                windowManager?.removeViewImmediate(overlayView)
            }
            if (!isAdded) {
                windowManager?.addView(overlayView, params)
                isAdded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        try {
            if (isAdded && overlayView?.parent != null) {
                windowManager?.removeViewImmediate(overlayView)
                isAdded = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateText(text: String) {
        textView?.text = text
    }
}
package com.example.turkcell

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityActions {

    // -----------------------------
    // MVP: Intent ile yönlendirmeler
    // -----------------------------
    fun openDialer(context: Context, phone: String? = null) {
        val uri = if (phone.isNullOrBlank()) Uri.parse("tel:") else Uri.parse("tel:$phone")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openSms(context: Context, phone: String? = null, body: String? = null) {
        val uri = if (phone.isNullOrBlank()) Uri.parse("smsto:") else Uri.parse("smsto:$phone")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (!body.isNullOrBlank()) putExtra("sms_body", body)
        }
        context.startActivity(intent)
    }

    fun openMhrs(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mhrs.gov.tr/")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // -----------------------------
    // Kayboldum modu (Accessibility)
    // -----------------------------
    fun doLostMode(service: AccessibilityService) {
        Log.d("MVP_ACTION", "Lost mode triggered")
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    // -----------------------------
    // (İlerisi için) Node yardımcıları
    // -----------------------------
    fun findFirstByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        val results = root.findAccessibilityNodeInfosByText(text)
        return results.firstOrNull()
    }

    fun findClickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var cur = node
        while (cur != null) {
            if (cur.isClickable) return cur
            cur = cur.parent
        }
        return null
    }

    fun focusNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        return node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
    }

    fun scrollForward(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }
}

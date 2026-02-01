package com.example.turkcell

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityActions {

    /**
     * Kayboldum modu: güvenli fallback
     * (Şimdilik sadece Home/Back. Yarın node bulma ekleriz.)
     */
    fun doLostMode(service: AccessibilityService) {
        Log.d("MVP_ACTION", "Lost mode triggered")
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    /**
     * Ekranda metin ara (basit)
     */
    fun findFirstByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        val results = root.findAccessibilityNodeInfosByText(text)
        return results?.firstOrNull()
    }

    /**
     * Tıklanabilir parent bul
     */
    fun findClickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var cur = node
        while (cur != null) {
            if (cur.isClickable) return cur
            cur = cur.parent
        }
        return null
    }

    /**
     * Node'a focus ver (vurgulama gibi düşün)
     */
    fun focusNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        return node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
    }

    /**
     * Kaydır (aşağı)
     */
    fun scrollForward(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }
}

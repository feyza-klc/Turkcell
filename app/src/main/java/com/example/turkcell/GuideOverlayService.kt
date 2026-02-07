package com.example.turkcell

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.util.Locale

class GuideOverlayService : Service(), TextToSpeech.OnInitListener {

    companion object {
        const val EXTRA_MODE = "mode" // "MHRS" veya "CALL"
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "guide_channel"
    }

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private lateinit var tts: TextToSpeech

    private lateinit var tvStep: TextView

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIF_ID, buildNotification())
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Mode’u intent’ten al, rehberi sıfırla
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: GuideScript.MODE_MHRS
        GuideScript.setGuideMode(mode)

        // Overlay açıksa ilk adımı güncelle/oku
        overlayView?.let {
            speakAndShow(GuideScript.current())
        }

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Guide", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rehberlik açık")
            .setContentText("Adım adım yönlendiriyorum.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_guide, null)

        tvStep = overlayView!!.findViewById(R.id.tvStep)
        val btnNext = overlayView!!.findViewById<Button>(R.id.btnNext)
        val btnPrev = overlayView!!.findViewById<Button>(R.id.btnPrev)
        val btnRepeat = overlayView!!.findViewById<Button>(R.id.btnRepeat)
        val btnRestart = overlayView!!.findViewById<Button>(R.id.btnRestart)
        val btnClose = overlayView!!.findViewById<Button>(R.id.btnClose)

        btnNext.setOnClickListener { speakAndShow(GuideScript.next()) }
        btnPrev.setOnClickListener { speakAndShow(GuideScript.prev()) }
        btnRepeat.setOnClickListener { speakAndShow(GuideScript.current()) }
        btnRestart.setOnClickListener { speakAndShow(GuideScript.restart()) }
        btnClose.setOnClickListener { stopSelf() }

        // sürükle-bırak (overlay'i taşı)
        overlayView!!.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0f
            private var lastY = 0f
            private var paramX = 0
            private var paramY = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = v.layoutParams as WindowManager.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        paramX = params.x
                        paramY = params.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = paramX + (event.rawX - lastX).toInt()
                        params.y = paramY + (event.rawY - lastY).toInt()
                        wm.updateViewLayout(v, params)
                        return true
                    }
                }
                return false
            }
        })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 200
        }

        wm.addView(overlayView, params)

        // İlk metin
        speakAndShow(GuideScript.current())
    }

    private fun speakAndShow(text: String) {
        val total = GuideScript.size()
        tvStep.text = "(${GuideScript.index + 1}/$total) $text"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide")
    }

    override fun onInit(status: Int) {
        tts.language = Locale("tr", "TR")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { wm.removeView(it) }
        overlayView = null
        tts.stop()
        tts.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

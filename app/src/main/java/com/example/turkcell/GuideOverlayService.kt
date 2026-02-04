package com.example.turkcell

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.util.Locale

class GuideOverlayService : Service(), TextToSpeech.OnInitListener {

    private lateinit var wm: WindowManager
    private var overlayView: View? = null

    private lateinit var tts: TextToSpeech
    private var speech: SpeechRecognizer? = null

    private lateinit var tvStep: TextView

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(this, this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        startForeground(1, buildNotification())

        showOverlay()
    }

    private fun buildNotification(): Notification {
        val channelId = "guide_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Guide", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rehberlik açık")
            .setContentText("MHRS adımlarında yardımcı oluyorum.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
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
        val btnMic = overlayView!!.findViewById<Button>(R.id.btnMic)
        val btnClose = overlayView!!.findViewById<Button>(R.id.btnClose)

        fun speakAndShow(text: String) {
            tvStep.text = "(${GuideScript.index + 1}/${GuideScript.mhrsSteps.size}) $text"
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide")
        }

        // İlk adımı oku
        speakAndShow(GuideScript.current())

        btnNext.setOnClickListener { speakAndShow(GuideScript.next()) }
        btnPrev.setOnClickListener { speakAndShow(GuideScript.prev()) }
        btnRepeat.setOnClickListener { speakAndShow(GuideScript.current()) }
        btnRestart.setOnClickListener { speakAndShow(GuideScript.restart()) }

        btnMic.setOnClickListener {
            startListeningOnce { spoken ->
                val t = spoken.lowercase()
                when {
                    t.contains("devam") || t.contains("ileri") -> speakAndShow(GuideScript.next())
                    t.contains("geri") -> speakAndShow(GuideScript.prev())
                    t.contains("tekrar") -> speakAndShow(GuideScript.current())
                    t.contains("baştan") || t.contains("sıfırla") -> speakAndShow(GuideScript.restart())
                    else -> speakAndShow("Anlayamadım. Devam, geri, tekrar veya baştan diyebilirsin.")
                }
            }
        }

        btnClose.setOnClickListener {
            stopSelf()
        }

        // Sürüklenebilir yap (overlay'i tutup sürükleme)
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
    }

    private fun startListeningOnce(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            onResult("")
            return
        }
        if (speech == null) speech = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        }

        val listener = SimpleRecognitionListener { text ->
            onResult(text)
        }
        speech?.setRecognitionListener(listener)
        speech?.startListening(intent)
    }

    override fun onInit(status: Int) {
        tts.language = Locale("tr", "TR")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { wm.removeView(it) }
        overlayView = null
        speech?.destroy()
        speech = null
        tts.stop()
        tts.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

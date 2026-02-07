package com.example.turkcell

import android.Manifest
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val REQ_AUDIO = 201
    private val REQ_SPEECH = 202

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var commandInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnMic).setOnClickListener {
            startVoiceInput()
        }


        statusText = findViewById(R.id.statusText)
        commandInput = findViewById(R.id.commandInput)

        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.btnRunCommand).setOnClickListener {
            val cmd = commandInput.text?.toString().orEmpty()
            runCommand(cmd)
        }

        // Butonlar kısa yollar
        findViewById<Button>(R.id.btnLost).setOnClickListener { runCommand("kayboldum") }
        findViewById<Button>(R.id.btnCall).setOnClickListener { runCommand("ara") }
        findViewById<Button>(R.id.btnMsg).setOnClickListener { runCommand("mesaj") }
        findViewById<Button>(R.id.btnMhrs).setOnClickListener { runCommand("mhrs") }

        findViewById<Button>(R.id.btnOpenAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            speak("Erişilebilirlik ayarlarını açtım. Asistan servisini etkinleştirmen gerekiyor.")
        }
    }
    private fun startVoiceInput() {
        // Mikrofon izni
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQ_AUDIO)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Komut söyle: kayboldum / ara / mesaj / mhrs")
        }
        startActivityForResult(intent, REQ_SPEECH)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput()
            } else {
                speak("Mikrofon izni olmadan sesli komut kullanılamaz.")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.firstOrNull().orEmpty()

            if (spoken.isNotBlank()) {
                commandInput.setText(spoken)
                speak("Anladım: $spoken")
                runCommand(spoken) // otomatik çalıştır
            } else {
                speak("Bir şey duyamadım. Tekrar dener misin?")
            }
        }
    }

    override fun onInit(status: Int) {
        tts.language = Locale("tr", "TR")
        speak("Merhaba. Asistan hazır.")
    }

    private fun runCommand(raw: String) {
        val type = CommandParser.parse(raw)

        when (type) {
            CommandType.LOST -> {
                statusText.text = "Durum: Kayboldum modu"

                val svc = MyAccessibilityService.instance
                if (svc == null) {
                    speak("Kayboldum modu için önce erişilebilirlik servisini açmalısın. Ayarlara yönlendiriyorum.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    return
                }

                speak("Kayboldum modu başlatıldı. Geri çıkıyorum ve ana ekrana dönüyorum.")
                AccessibilityActions.doLostMode(svc)
            }

            CommandType.CALL -> {
                statusText.text = "Durum: Arama modu"
                speak("Arama ekranını açıyorum. Rehberlik penceresi açıldı.")
                AccessibilityActions.openDialer(this)

                // Overlay izni kontrol (MHRS'de yaptığın gibi)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        val i = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(i)
                        speak("Devam etmek için diğer uygulamaların üzerinde göster izni vermelisin.")
                        return
                    }
                }

                val svcIntent = Intent(this, GuideOverlayService::class.java).apply {
                    putExtra(GuideOverlayService.EXTRA_MODE, GuideScript.MODE_CALL)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(svcIntent)
                } else {
                    startService(svcIntent)
                }
            }


            CommandType.MESSAGE -> {
                statusText.text = "Durum: Mesaj modu"
                speak("Mesaj ekranını açıyorum. Rehberlik penceresi açıldı.")
                AccessibilityActions.openSms(this)

                // Overlay izni kontrolü
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        val i = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(i)
                        speak("Devam etmek için diğer uygulamaların üzerinde göster izni vermelisin.")
                        return
                    }
                }

                val svcIntent = Intent(this, GuideOverlayService::class.java).apply {
                    putExtra(GuideOverlayService.EXTRA_MODE, GuideScript.MODE_MESSAGE)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(svcIntent)
                } else {
                    startService(svcIntent)
                }
            }

            CommandType.MHRS -> {
                statusText.text = "Durum: MHRS modu"
                speak("MHRS sitesini açıyorum. Rehberlik penceresi açıldı.")
                AccessibilityActions.openMhrs(this)

                // Overlay izni yoksa ayara götür
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        val i = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(i)
                        speak("Devam etmek için diğer uygulamaların üzerinde göster izni vermelisin.")
                        return
                    }
                }

                val svcIntent = Intent(this, GuideOverlayService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(svcIntent)
                } else {
                    startService(svcIntent)
                }
            }


            CommandType.UNKNOWN -> {
                statusText.text = "Durum: Anlaşılmadı"
                speak("Bunu anlayamadım. Kayboldum, ara, mesaj veya mhrs diyebilirsin.")
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}

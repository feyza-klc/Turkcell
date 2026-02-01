package com.example.turkcell

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

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var commandInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        commandInput = findViewById(R.id.commandInput)

        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.btnRunCommand).setOnClickListener {
            val cmd = commandInput.text?.toString() ?: ""
            runCommand(cmd)
        }

        findViewById<Button>(R.id.btnLost).setOnClickListener { runCommand("kayboldum") }
        findViewById<Button>(R.id.btnCall).setOnClickListener { runCommand("ara") }
        findViewById<Button>(R.id.btnMsg).setOnClickListener { runCommand("mesaj") }
        findViewById<Button>(R.id.btnMhrs).setOnClickListener { runCommand("mhrs") }

        findViewById<Button>(R.id.btnOpenAccessibilitySettings).setOnClickListener {
            // Kullanıcıyı direkt Erişilebilirlik ayarına götür
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            speak("Erişilebilirlik ayarlarını açtım. Asistan servisini etkinleştirmen gerekiyor.")
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
                speak("Arama modu. Kimi aramak istiyorsun? Şimdilik rehberlik yapacağım.")
            }

            CommandType.MESSAGE -> {
                statusText.text = "Durum: Mesaj modu"
                speak("Mesaj modu. Kime mesaj atacaksın? Şimdilik rehberlik yapacağım.")
            }

            CommandType.MHRS -> {
                statusText.text = "Durum: MHRS modu"
                speak("MHRS modu. Seni randevu ekranına yönlendireceğim. Servis açık olmalı.")
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

package com.example.turkcell

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

class SimpleRecognitionListener(
    private val onFinal: (String) -> Unit
) : RecognitionListener {

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        onFinal(matches?.firstOrNull().orEmpty())
    }

    override fun onError(error: Int) { onFinal("") }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}

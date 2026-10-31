package com.inspiredandroid.kai.tts

import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.errors.TextToSpeechSynthesisInterruptedError

interface TextToSpeechService {
    val isAvailable: Boolean
    suspend fun speak(text: String)
    fun stop()
    fun pause()
    fun resume()
}

class TextToSpeechServiceImpl(
    private val textToSpeech: TextToSpeechInstance?,
) : TextToSpeechService {
    override val isAvailable: Boolean = textToSpeech != null

    override suspend fun speak(text: String) {
        if (textToSpeech == null) return
        try {
            textToSpeech.say(text = text)
        } catch (_: TextToSpeechSynthesisInterruptedError) {
            // Expected interruption
        } catch (_: Exception) {
            // Handle gracefully
        }
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun pause() {
        textToSpeech?.stop()
    }

    override fun resume() {
    }
}

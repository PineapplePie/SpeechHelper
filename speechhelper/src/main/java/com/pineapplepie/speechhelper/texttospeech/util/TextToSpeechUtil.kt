package com.pineapplepie.speechhelper.texttospeech.util

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

internal inline fun TextToSpeech.addUtteranceListener(
    crossinline start: UtteranceCallback = { _ -> },
    crossinline done: UtteranceCallback = { _ -> },
    crossinline error: UtteranceCallback = { _ -> },
): UtteranceProgressListener {
    object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            start(utteranceId)
        }

        override fun onDone(utteranceId: String?) {
            done(utteranceId)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            error(utteranceId)
        }

        @Deprecated("Deprecated in Java", ReplaceWith("error(utteranceId)"))
        override fun onError(utteranceId: String?) {
            error(utteranceId)
        }
    }.also {
        setOnUtteranceProgressListener(it)
        return it
    }
}

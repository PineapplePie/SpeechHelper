package com.pineapplepie.speechhelper.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import com.pineapplepie.speechhelper.texttospeech.state.InitializationState
import com.pineapplepie.speechhelper.texttospeech.util.InitializationCallback

internal object TextToSpeechFactory {

    fun createTextToSpeech(
        context: Context,
        enginePackageName: String?,
        callbackResult: InitializationCallback
    ): TextToSpeech {
        val initializationListener = TextToSpeech.OnInitListener { status ->
            callbackResult(status.mapInitializationStatus())
        }

        return if (enginePackageName == null) {
            TextToSpeech(context.applicationContext, initializationListener)
        } else {
            TextToSpeech(context.applicationContext, initializationListener, enginePackageName)
        }
    }

    private fun Int.mapInitializationStatus() = when (this) {
        TextToSpeech.SUCCESS -> InitializationState.Success
        TextToSpeech.ERROR -> InitializationState.Error
        // according to docs the third status STOPPED shouldn't occur at all
        else -> InitializationState.None
    }
}
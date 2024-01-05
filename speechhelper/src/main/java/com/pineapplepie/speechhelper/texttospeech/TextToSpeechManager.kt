package com.pineapplepie.speechhelper.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.pineapplepie.speechhelper.texttospeech.state.InitializationState
import com.pineapplepie.speechhelper.texttospeech.state.SpeakingState
import com.pineapplepie.speechhelper.texttospeech.util.GENERAL_TTS_ERROR
import com.pineapplepie.speechhelper.texttospeech.util.NO_ENGINES_AVAILABLE_ERROR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextToSpeechManager {

    constructor(context: Context) {
        init(context, null)
    }

    constructor(context: Context, enginePackageName: String?) : this(context) {
        init(context, enginePackageName)
    }

    private val _speakingStatus = MutableStateFlow<SpeakingState>(SpeakingState.None)
    val speakingStatus: StateFlow<SpeakingState> = _speakingStatus

    private var initializationState: InitializationState = InitializationState.None

    private val sentenceQueue: ArrayDeque<Sentence> = ArrayDeque()

    private var _textToSpeech: TextToSpeech? = null
    private val textToSpeech: TextToSpeech
        get() = requireNotNull(_textToSpeech) {
            "Text-to-speech object should be already initialized!"
        }

    private fun init(context: Context, enginePackageName: String?) {
        _textToSpeech ?: return
        _textToSpeech = TextToSpeechFactory.createTextToSpeech(context, enginePackageName) {
            this.initializationState = it
            if (it is InitializationState.Success) {
                requestAudioFocus()
                fetchLanguages()
            } else {
                Log.e(TAG, "TTS initialization has failed")
            }
        }
    }

    private fun requestAudioFocus() {

    }

    private fun fetchLanguages() {

    }

    private fun checkIfInitialized(): Boolean = when {
        _textToSpeech == null -> {
            Log.d(TAG, "You must initialize (call init()) your text-to-speech manager!")
            false
        }

        initializationState is InitializationState.Error -> {
            Log.e(TAG, "TTS initialization has failed with error: ${retrieveErrorMessage()}")
            false
        }

        else -> true
    }

    private fun retrieveErrorMessage(): String = if (textToSpeech.engines.isEmpty()) {
        NO_ENGINES_AVAILABLE_ERROR
    } else {
        GENERAL_TTS_ERROR
    }
}

private val TAG = TextToSpeechManager::class.java.simpleName
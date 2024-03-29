package com.pineapplepie.speechhelper.texttospeech

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.pineapplepie.speechhelper.texttospeech.audiofocus.AudioFocusManager
import com.pineapplepie.speechhelper.texttospeech.state.InitializationState
import com.pineapplepie.speechhelper.texttospeech.state.SpeakingState
import com.pineapplepie.speechhelper.texttospeech.util.addUtteranceListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechManager {

    constructor(context: Context) {
        init(context, null)
    }

    constructor(context: Context, enginePackageName: String?) : this(context) {
        init(context, enginePackageName)
    }

    private val _speakingStatus = MutableStateFlow<SpeakingState>(SpeakingState.None)
    val speakingStatus: StateFlow<SpeakingState> = _speakingStatus

    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.None)
    val initializationState: StateFlow<InitializationState> = _initializationState

    private val sentenceQueue: ArrayDeque<Sentence> = ArrayDeque()

    private lateinit var audioFocusManager: AudioFocusManager

    private var _textToSpeech: TextToSpeech? = null
    private val textToSpeech: TextToSpeech
        get() = requireNotNull(_textToSpeech) {
            "Text-to-speech object should be already initialized!"
        }

    fun setText(text: String): Boolean {
        if (!checkIfInitialized()) return false
        val sentences = text.split(*SENTENCE_SEPARATORS).filter { it.isNotBlank() }
        sentenceQueue.clear()
        return sentenceQueue.addAll(sentences.map { Sentence(it) })
    }

    fun setLanguage(locale: Locale): Boolean {
        if (!checkIfInitialized()) return false
        val status = textToSpeech.isLanguageAvailable(locale)
        val isAvailable = status != TextToSpeech.LANG_NOT_SUPPORTED && status != TextToSpeech.LANG_MISSING_DATA
        if (isAvailable) textToSpeech.language = locale
        return isAvailable
    }

    fun play() {
        if (!checkIfInitialized() || sentenceQueue.isEmpty()) return
        requestAudioFocus()
        readNextLine()
    }

    fun pause() {
        if (!checkIfInitialized()) return
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            abandonAudioFocus()
            _speakingStatus.tryEmit(SpeakingState.Paused)
        }
    }

    fun isSpeaking(): Boolean {
        if (!checkIfInitialized()) return false
        return textToSpeech.isSpeaking
    }

    fun isPaused(): Boolean {
        if (!checkIfInitialized()) return false
        return speakingStatus.value is SpeakingState.Paused
    }

    fun isInitialized(): Boolean = checkIfInitialized()

    fun release() {
        if (!checkIfInitialized()) return
        if (textToSpeech.isSpeaking) textToSpeech.stop()
        abandonAudioFocus()
        sentenceQueue.clear()
        textToSpeech.shutdown()
        _textToSpeech = null
    }

    private fun init(context: Context, enginePackageName: String?) {
        if (_textToSpeech != null) return
        audioFocusManager = AudioFocusManager(context.getSystemService(AUDIO_SERVICE) as AudioManager) { pause() }
        _textToSpeech = TextToSpeechFactory.createTextToSpeech(context, enginePackageName) {
            _initializationState.tryEmit(InitializationState.Success)
            if (it is InitializationState.Success) {
                listenToProgress()
                requestAudioFocus()
            } else {
                Log.e(TAG, "TTS initialization has failed")
            }
        }
    }

    private fun requestAudioFocus() {
        audioFocusManager.requestAudioFocus()
    }

    private fun abandonAudioFocus() {
        audioFocusManager.abandonAudioFocus()
    }

    private fun readNextLine() {
        if (sentenceQueue.isEmpty()) {
            _speakingStatus.tryEmit(SpeakingState.Finished)
            return
        }

        _speakingStatus.tryEmit(SpeakingState.Speaking)
        val sentence = sentenceQueue.first()
        textToSpeech.speak(
            sentence.text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            sentence.utteranceId,
        )
    }

    private fun listenToProgress() {
        textToSpeech.addUtteranceListener(start = {
            _speakingStatus.tryEmit(SpeakingState.Speaking)
        }, done = {
            sentenceQueue.removeFirst()
            readNextLine()
        })
    }

    private fun checkIfInitialized(): Boolean = when {
        _textToSpeech == null -> {
            Log.d(TAG, "You must initialize (call init()) your text-to-speech manager!")
            false
        }

        initializationState.value is InitializationState.Error -> {
            Log.e(TAG, "TTS initialization has failed with error: ${retrieveErrorMessage()}")
            false
        }

        initializationState.value is InitializationState.None -> {
            Log.e(TAG, "TTS is not initialized yet, please wait for the callback")
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

internal val SENTENCE_SEPARATORS = arrayOf(".", ",", "?", "!", "¿", "¡")
internal const val NO_ENGINES_AVAILABLE_ERROR = "No engines available, you need to install a TTS app"
internal const val GENERAL_TTS_ERROR = "General initialization error"
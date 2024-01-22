package com.pineapplepie.speechhelper.texttospeech

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.pineapplepie.speechhelper.texttospeech.audiofocus.AudioFocusManager
import com.pineapplepie.speechhelper.texttospeech.state.InitializationState
import com.pineapplepie.speechhelper.texttospeech.state.SpeakingState
import com.pineapplepie.speechhelper.texttospeech.util.GENERAL_TTS_ERROR
import com.pineapplepie.speechhelper.texttospeech.util.NO_ENGINES_AVAILABLE_ERROR
import com.pineapplepie.speechhelper.texttospeech.util.SENTENCE_SEPARATORS
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

    private var initializationState: InitializationState = InitializationState.None

    private val sentenceQueue: ArrayDeque<Sentence> = ArrayDeque()

    private lateinit var audioFocusManager: AudioFocusManager

    private var _textToSpeech: TextToSpeech? = null
    private val textToSpeech: TextToSpeech
        get() = requireNotNull(_textToSpeech) {
            "Text-to-speech object should be already initialized!"
        }

    fun setText(text: String) {
        if (!checkIfInitialized()) return
        val sentences = text.split(*SENTENCE_SEPARATORS).filter { it.isNotBlank() }
        sentenceQueue.clear()
        sentenceQueue.addAll(sentences.map { Sentence(it, it.hashCode().toString()) })
    }

    fun setLanguage(locale: Locale) {
        if (!checkIfInitialized()) return
        val status = textToSpeech.isLanguageAvailable(locale)
        val isAvailable = status != TextToSpeech.LANG_NOT_SUPPORTED && status != TextToSpeech.LANG_MISSING_DATA
        if (isAvailable) textToSpeech.language = locale
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
            requestAudioFocus()
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
            this.initializationState = it
            if (it is InitializationState.Success) {
                setLanguage(Locale.getDefault())
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
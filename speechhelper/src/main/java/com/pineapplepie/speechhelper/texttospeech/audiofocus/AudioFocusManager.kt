package com.pineapplepie.speechhelper.texttospeech.audiofocus

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.pineapplepie.speechhelper.texttospeech.util.Action

internal class AudioFocusManager(
    private val audioManager: AudioManager,
    private val focusLostCallback: Action,
) {

    private var currentAudioFocus = AUDIOFOCUS_NONE

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focus ->
        currentAudioFocus = focus
        if (focus in lostFocusStates) focusLostCallback()
    }

    @delegate:SuppressLint("NewApi")
    private val audioFocusRequest: AudioFocusRequest by lazy {
        val focus = AudioManager.AUDIOFOCUS_GAIN
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setLegacyStreamType(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        AudioFocusRequest.Builder(focus)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()
    }

    fun requestAudioFocus() {
        if (currentAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }
}

internal val lostFocusStates = arrayOf(
    AudioManager.AUDIOFOCUS_LOSS,
    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
)
internal const val AUDIOFOCUS_NONE = -1

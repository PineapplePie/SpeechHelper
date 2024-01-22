package com.pineapplepie.speechhelper.texttospeech.util

import android.media.AudioManager


internal const val NO_ENGINES_AVAILABLE_ERROR = "No engines available, you need to install a TTS app"
internal const val GENERAL_TTS_ERROR = "General initialization error"

internal val SENTENCE_SEPARATORS = arrayOf(".", ",", "?", "!", "¿", "¡")

internal val lostFocusStates = arrayOf(
    AudioManager.AUDIOFOCUS_LOSS,
    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
)

internal const val AUDIOFOCUS_NONE = -1

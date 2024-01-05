package com.pineapplepie.speechhelper.texttospeech.state

sealed class SpeakingState {

    data object None : SpeakingState()

    data object Speaking : SpeakingState()

    data object Paused : SpeakingState()

    data object Finished : SpeakingState()
}

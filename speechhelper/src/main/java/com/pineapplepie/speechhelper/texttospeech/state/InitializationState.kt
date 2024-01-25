package com.pineapplepie.speechhelper.texttospeech.state

sealed class InitializationState {

    data object None : InitializationState()

    data object Success : InitializationState()

    data object Error : InitializationState()
}

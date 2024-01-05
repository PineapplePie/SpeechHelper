package com.pineapplepie.speechhelper.texttospeech.state

internal sealed class InitializationState {

    data object None : InitializationState()

    data object Success : InitializationState()

    data object Error : InitializationState()
}

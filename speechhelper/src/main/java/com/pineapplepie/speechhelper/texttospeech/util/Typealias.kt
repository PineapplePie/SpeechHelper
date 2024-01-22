package com.pineapplepie.speechhelper.texttospeech.util

import com.pineapplepie.speechhelper.texttospeech.state.InitializationState

internal typealias UtteranceCallback = (utteranceId: String?) -> Unit

internal typealias InitializationCallback = (InitializationState) -> Unit

internal typealias Action = () -> Unit
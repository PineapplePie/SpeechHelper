package com.pineapplepie.speechhelper.texttospeech

import java.util.UUID

internal data class Sentence(val text: String, val utteranceId: String = UUID.randomUUID().toString())

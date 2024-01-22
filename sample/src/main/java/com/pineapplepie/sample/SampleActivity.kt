package com.pineapplepie.sample

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pineapplepie.sample.databinding.ActivitySampleBinding
import com.pineapplepie.speechhelper.texttospeech.TextToSpeechManager
import com.pineapplepie.speechhelper.texttospeech.state.SpeakingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    private lateinit var textToSpeechManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextToSpeech()
        setupViews()
    }

    private fun setupTextToSpeech() {
        textToSpeechManager = TextToSpeechManager(this)
        textToSpeechManager.speakingStatus.observe { status ->
            binding.statusText.text = SpannableStringBuilder()
                .append(getString(R.string.sample_status_text_formatted))
                .bold { append(" $status") }

            if (status is SpeakingState.Paused) {
                binding.pauseButton.text = getString(R.string.sample_resume_button_text)
            } else {
                binding.pauseButton.text = getString(R.string.sample_pause_button_text)
            }
        }
    }

    private fun setupViews() = with(binding) {
        readButton.setOnClickListener { readText() }
        pauseButton.setOnClickListener { onPauseClicked() }
    }

    private fun onPauseClicked() {
        if (textToSpeechManager.isPaused()) {
            textToSpeechManager.play()
        } else {
            textToSpeechManager.pause()
        }
    }

    private fun readText() {
        val text = binding.inputText.text?.toString()
        if (text.isNullOrBlank()) {
            Toast.makeText(this, R.string.sample_warning_toast_text, Toast.LENGTH_SHORT).show()
            return
        }

        textToSpeechManager.setText(text)
        textToSpeechManager.play()
    }

    override fun onDestroy() {
        super.onDestroy()

        textToSpeechManager.release()
    }

    private fun <T> Flow<T>.observe(callback: (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                this@observe.collect { callback(it) }
            }
        }
    }
}

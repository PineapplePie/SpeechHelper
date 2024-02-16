# SpeechHelper

![Version](https://img.shields.io/badge/version-1.0.0rc-blue.svg)
![minSdkVersion](https://img.shields.io/badge/minSdk-21-red.svg)
![compileSdkVersion](https://img.shields.io/badge/compileSdkVersion-34-green.svg)

SpeechHelper is an Android text-to-speech (TTS) library that simplifies the process of reading aloud any given text.

Main points:
- Supports pausing/resuming functionality and deals with the audio focus by itself;
- Uses a pre-installed engine or a provided one instead;
- Kotlin-based, flow-based and lightweight.

## Download via Jitpack

Add it in your root build.gradle at the end of repositories:

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency

```
dependencies {
    implementation 'com.github.PineapplePie:SpeechHelper:1.0.0rc'
}
```

## Usage

### Important point!
Your device should have a pre-installed or available text-to-speech engine. Usually it's a separate app, and mostly all Android devices do have it by default. If your device or emulator lacks it, please google, download it as an APK/from Play Store. 

For a full example take a look at the [sample module](https://github.com/PineapplePie/SpeechHelper/tree/develop/sample/src/main/java/com/pineapplepie/sample).


### Step 1.
Define a text-to-speech variable:

```
val textToSpeechManager = TextToSpeechManager(this)
// or
val textToSpeechManager = TextToSpeechManager(this, "YOUR_ENGINE_PACKAGE_NAME")
```

### Step 2. Wait until the initialization is completed. 
The process is asynchronous. The library uses flows, so you should subscribe with a [lifecycle-aware coroutine scope](https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware).
Normally, you wouldn't get an error here, if your initialization is failed, please check the troubleshooting section down below.

```
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        textToSpeechManager.initializationState.observe { status ->
            if (status is InitializationState.Success) {
                // your further setup code here
            }
        }
    }
}
```


### Step 3. Setup text, play it, pause if needed.
The initial framework does not support pausing, so here I'm splitting the sentences by punctuation marks like .,! etc. So if you paused and resumed the speech, it would start reading a sentence again from the latest punctuation mark.

```
textToSpeechManager.setText("I like cats. What do you think about their fluffy paws?")

// play it
textToSpeechManager.play()

// pause it
textToSpeechManager.pause()

// resume it
textToSpeechManager.play()
```

### Step 4. Observe a speaking status if needed.

```
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        textToSpeechManager.speakingStatus.observe { status ->
            when (status) {
                is SpeakingState.Paused -> {
                    // wow, it's paused, magic!
                }
                is SpeakingState.Speaking -> {
                   // wow, it knows how to talk!
                }
                is SpeakingState.Finished -> {
                  // okay, this joke season is done, the speech is finished as  well...
                }
                is SpeakingState.None -> {
                  // it means that you haven't even started the speech
                }
            }
        }
    }
}
```

### Step 5. Release all resources when you don't need them anymore in onDestroy() or any other suitable place.

```
textToSpeechManager.release()
```

### Step 6. Voilà!

## Troubleshooting

If the text-to-speech functionality does not work, please check the logs. You will see there an error log with a tag "TextToSpeechManager". It could be either an engine missing or you're trying to use the framework before it's initialized.

## Coming soon

Voice-to-text functionality is planned for the near future :)

## License

SpeechHelper is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

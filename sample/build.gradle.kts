plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = Dependencies.sampleId
    compileSdk = Dependencies.maxSdkVersion

    defaultConfig {
        applicationId = Dependencies.sampleId
        minSdk = Dependencies.minSdkVersion
        targetSdk = Dependencies.maxSdkVersion
        versionCode = Dependencies.versionCode
        versionName = Dependencies.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = Dependencies.jvmTarget
    }
}

dependencies {
    implementation(project(":speechhelper"))

    implementation("androidx.core:core-ktx:${Dependencies.coreVersion}")
    implementation("androidx.appcompat:appcompat:${Dependencies.compatVersion}")
    implementation("com.google.android.material:material:${Dependencies.materialVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Dependencies.lifecycleVersion}")
    testImplementation("junit:junit:${Dependencies.jUnitVersion}")
}
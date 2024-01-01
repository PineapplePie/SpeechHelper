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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = Dependencies.jvmTarget
    }
}

dependencies {

    implementation("androidx.core:core-ktx:${Dependencies.coreVersion}")
    implementation("androidx.appcompat:appcompat:${Dependencies.compatVersion}")
    implementation("com.google.android.material:material:${Dependencies.materialVersion}")
    testImplementation("junit:junit:${Dependencies.jUnitVersion}")
}
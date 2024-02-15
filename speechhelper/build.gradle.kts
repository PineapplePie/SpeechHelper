plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = Dependencies.libId
    compileSdk = Dependencies.maxSdkVersion

    defaultConfig {
        minSdk = Dependencies.minSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {

    implementation("androidx.core:core-ktx:${Dependencies.coreVersion}")
    implementation("androidx.appcompat:appcompat:${Dependencies.compatVersion}")
    testImplementation("junit:junit:${Dependencies.jUnitVersion}")
}


publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = Dependencies.groupId
            artifactId = Dependencies.libArtifact
            version = Dependencies.libVersionName

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}


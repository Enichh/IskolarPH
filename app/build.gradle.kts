plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.iskolarphh"
    compileSdk = 36 // Updated to a simpler syntax if possible, but keeping consistency

    // Explicitly set the compileSdk version as provided in the original file
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.iskolarphh"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Configure the Java toolchain to ensure a valid JDK is used, 
// avoiding references to missing or temporary extension JDKs.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    implementation(libs.androidx.room.runtime)
    androidTestImplementation(libs.ext.junit)
    annotationProcessor(libs.androidx.room.compiler)
    androidTestImplementation(libs.espresso.core)
}
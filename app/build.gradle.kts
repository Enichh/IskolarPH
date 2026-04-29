import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Get API configuration from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
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

    signingConfigs {
        create("release") {
            val keystoreFile = file("../keystore.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = localProperties.getProperty("KEYSTORE_PASSWORD", "")
                keyAlias = localProperties.getProperty("KEY_ALIAS", "")
                keyPassword = localProperties.getProperty("KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            val keystoreFile = file("../keystore.jks")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Configure custom APK naming
    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val versionName = defaultConfig.versionName
                val buildType = variant.buildType
                output.outputFileName.set("ISKOLARPH-v${versionName}-${buildType}.apk")
            }
        }
    }
    buildFeatures {
        buildConfig = true
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
    implementation(libs.coordinatorlayout)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    implementation(libs.androidx.room.runtime)
    androidTestImplementation(libs.ext.junit)
    annotationProcessor(libs.androidx.room.compiler)
    androidTestImplementation(libs.espresso.core)
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.markwon.core)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.tables)

    // Glide for image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // LeakCanary for memory leak detection (debug only)
    // Temporarily disabled due to main thread blocking during startup
    // debugImplementation(libs.leakcanary.android)

    // Unit Testing
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}

// API Configuration
android.defaultConfig.buildConfigField("String", "LONGCAT_API_KEY", "\"${localProperties.getProperty("LONGCAT_API_KEY", "")}\"")
android.defaultConfig.buildConfigField("String", "LONGCAT_API_BASE_URL", "\"${localProperties.getProperty("LONGCAT_API_BASE_URL", "https://api.example.com")}\"")

// Supabase Configuration
android.defaultConfig.buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
android.defaultConfig.buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", "")}\"")

plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.javajedis.bookit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.javajedis.bookit"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha01")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.6.0-alpha01")
    androidTestImplementation ("com.android.support.test.espresso:espresso-intents:3.0.2")
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.4.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.27")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("androidx.tracing:tracing:1.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.0-alpha01")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0-alpha03")
}
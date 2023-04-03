plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 33

    defaultConfig {
        namespace = "com.github.fengdai.compose.androidview.issue"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$buildDir/compose",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$buildDir/compose"
        )
    }

    val samples by signingConfigs.creating {
        storeFile(file("../samples.keystore"))
        storePassword("javascript")
        keyAlias("javascript")
        keyPassword("javascript")
    }

    buildTypes {
        val debug by getting {
            signingConfig = samples
        }
        val release by getting {
            signingConfig = samples
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    implementation("androidx.core:core-ktx:1.9.0")
//    implementation("com.telefonica:nestedscrollwebview:0.1.1")
    implementation("com.google.accompanist:accompanist-webview:0.28.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
}

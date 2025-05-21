import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties()
file("local.properties").inputStream().use {
    localProperties.load(it)
}

val admobBannerAdUnitIdDebug: String =
    localProperties.getProperty("ADMOB_BANNER_AD_UNIT_ID_DEBUG") ?: ""
val admobBannerAdUnitIdRelease: String =
    localProperties.getProperty("ADMOB_BANNER_AD_UNIT_ID_RELEASE") ?: ""

val admobAppIdDebug: String =
    localProperties.getProperty("ADMOB_APP_ID_DEBUG") ?: ""
val admobAppIdRelease: String =
    localProperties.getProperty("ADMOB_APP_ID_RELEASE") ?: ""

android {
    namespace = "com.erikbalthazar.admobbanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.erikbalthazar.admobbanner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdDebug
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "ADMOB_BANNER_AD_UNIT_ID",
                "\"$admobBannerAdUnitIdDebug\""
            )
            manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdDebug
        }
        release {
            buildConfigField(
                "String",
                "ADMOB_BANNER_AD_UNIT_ID",
                "\"$admobBannerAdUnitIdRelease\""
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdRelease
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkAllWarnings = true
        abortOnError = true
        warningsAsErrors = true
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.hilt)
    implementation(libs.firebase.crashlytics.ktx)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
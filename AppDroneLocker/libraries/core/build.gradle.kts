plugins {
    id("com.android.library")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.delivery.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        buildConfigField("String", "MAPTILER_API_KEY", "\"JVNoUqIi4qLqLWtEiEmG\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
//            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }

    val javaVersion = rootProject.extra["versionJava"]
    java {
        sourceCompatibility = javaVersion as JavaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {

    implementation(libs.bundles.common)
    kapt(libs.hiltSupport)

    //navigation
    implementation(libs.bundles.navigation)

    //network
    implementation(libs.bundles.network)

    kapt(libs.liveData)

    implementation(libs.glide)
    kapt(libs.glideCompiler)

    // Maps Utils for polyline drawing
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    implementation(libs.maptiler)

}

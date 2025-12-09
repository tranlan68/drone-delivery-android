plugins {
    id("com.android.library")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.delivery.setting"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        buildConfigField("String", "MAPTILER_API_KEY", "\"JVNoUqIi4qLqLWtEiEmG\"")
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        release {
            isMinifyEnabled = true
//            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
    }

    buildFeatures {
        dataBinding = true
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

    implementation(project(":libraries:core"))
    implementation(project(":libraries:database"))
    implementation(project(":libraries:permission"))

    // appcompat
    implementation(libs.bundles.common)

    kapt(libs.hiltSupport)

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Maps Utils for polyline drawing
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    implementation(libs.maptiler)

    // Paging3
    implementation(libs.paging)

    kapt(libs.liveData)

    // navigation
    implementation(libs.bundles.navigation)

    // Loading Image
    implementation(libs.glide)
    kapt(libs.glideCompiler)
    // Network
    implementation(libs.bundles.network)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("junit:junit:4.13.2")
}

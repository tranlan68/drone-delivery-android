// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("versionJava", JavaVersion.VERSION_1_8)
    }
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.androidGradle)
        classpath(libs.kotlinGradle)
        classpath(libs.hiltGradle)
        classpath(libs.gms)
    }
}


allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task("clean") {
    delete(project.buildDir)
}

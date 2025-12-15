// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.Project
buildscript {

    extra.apply {
        set("versionJava", JavaVersion.VERSION_1_8)
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

subprojects {

    pluginManager.withPlugin("com.android.application") {
        configureAndroidLint()
        configureJacoco()
    }
    pluginManager.withPlugin("com.android.library") {
        configureAndroidLint()
        configureJacoco()
    }
}

tasks.register("lintAll") {
    group = "verification"
    description = "Run Android lint for all modules"
    dependsOn(subprojects.map { "${it.path}:lint" })
}

tasks.register("ktlintCheck") {
    group = "verification"
    description = "Run ktlint check for all modules"
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}

tasks.register("ktlintFormat") {
    group = "formatting"
    description = "Run ktlint format for all modules"
    dependsOn(subprojects.map { "${it.path}:ktlintFormat" })
}

tasks.register("testCoverage") {
    group = "verification"
    description = "Run unit tests with coverage for all modules"
}

tasks.register("testDebugUnitTestCoverage") {
    group = "verification"
    description = "Run unit tests with coverage and generate report for all modules"
}

tasks.register("ci") {
    group = "verification"
    description = "Run all CI checks: lint, ktlint, tests, and coverage"
    dependsOn(
        "lintAll",
        "ktlintCheck",
        "testDebugUnitTest",
        "testDebugUnitTestCoverage"
    )
    doLast {
        println("✅ All CI checks completed successfully!")
    }
}

subprojects {
    afterEvaluate {
        val testTask = tasks.findByName("testDebugUnitTest")
        val coverageTask = tasks.findByName("testDebugUnitTestCoverage")
        if (testTask != null) {
            rootProject.tasks.named("testCoverage") {
                if (coverageTask != null) {
                    dependsOn(coverageTask)
                } else {
                    dependsOn(testTask)
                }
            }
            rootProject.tasks.named("testDebugUnitTestCoverage") {
                if (coverageTask != null) {
                    dependsOn(coverageTask)
                } else {
                    dependsOn(testTask)
                }
            }
            rootProject.tasks.named("testDebugUnitTest") {
                dependsOn(testTask)
            }
        }
    }
}

tasks.register("clean") {
    delete(project.buildDir)
}

tasks.register("testDebugUnitTest") {
    group = "verification"
    description = "Run unit tests for all modules"
}

fun Project.configureAndroidLint() {
    val extension = extensions.findByName("android")
    if (extension is com.android.build.api.dsl.CommonExtension<*, *, *, *, *>) {
        extension.lint {
            abortOnError = true
            warningsAsErrors = false
            checkAllWarnings = true
            explainIssues = true
            htmlReport = true
            xmlReport = true
        }
    }
}

fun Project.configureJacoco() {
    apply(plugin = "jacoco")
    val extension = extensions.findByName("android")
    if (extension is com.android.build.api.dsl.CommonExtension<*, *, *, *, *>) {
        extension.buildTypes {
            getByName("debug") {
                enableUnitTestCoverage = true
            }
        }
    }
    afterEvaluate {
        tasks.withType<org.gradle.api.tasks.testing.Test> {
            extensions.configure<org.gradle.testing.jacoco.plugins.JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
        tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport> {
            dependsOn("testDebugUnitTest")
            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }
            val androidExtension = extensions.findByName("android")
            if (androidExtension is com.android.build.api.dsl.CommonExtension<*, *, *, *, *>) {
                executionData.setFrom(
                    fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec")
                )
                val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
                    exclude(
                        "**/R.class",
                        "**/R\$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "android/**/*.*",
                        "**/databinding/**/*.*",
                        "**/android/databinding/**/*.*",
                        "**/BR.*",
                        "**/BR\$*.*",
                        "**/*\$ViewInjector*.*",
                        "**/*Dagger*.*",
                        "**/*MembersInjector*.*",
                        "**/*_Factory*.*",
                        "**/*_Provide*Factory*.*",
                        "**/*Extensions*.*",
                        "**/*Module*.*",
                        "**/*Component*.*",
                        "**/*SubComponent*.*",
                        "**/*SubComponent\$Builder*.*",
                        "**/*Module\$Factory*.*",
                    )
                }
                classDirectories.setFrom(javaClasses)
                val mainSourceSet = androidExtension.sourceSets.getByName("main")
                sourceDirectories.setFrom(
                    files(
                        mainSourceSet.java.srcDirs(),
                        "src/main/kotlin"
                    )
                )
            }
        }
        val coverageReportTask = tasks.findByName("createDebugUnitTestCoverageReport")
        if (coverageReportTask != null) {
            tasks.register("testDebugUnitTestCoverage") {
                group = "verification"
                description = "Run unit tests with coverage and generate report"
                dependsOn("testDebugUnitTest", coverageReportTask)
            }
        }
    }
}

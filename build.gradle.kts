buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
plugins {
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.gradle.cachefix).apply(false)
    alias(libs.plugins.compose.compiler) apply false
}

tasks {
    wrapper {
        gradleVersion = libs.versions.gradle.get()
        distributionType = Wrapper.DistributionType.BIN
    }
}
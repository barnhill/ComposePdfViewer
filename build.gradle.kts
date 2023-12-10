buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
val gradleVersion: String by extra

plugins {
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.gradle.cachefix).apply(false)
}

tasks {
    wrapper {
        gradleVersion = "8.5"
        distributionType = Wrapper.DistributionType.BIN
    }
}
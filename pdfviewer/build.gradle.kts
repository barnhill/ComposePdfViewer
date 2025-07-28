plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    id("kotlin-parcelize")
}

val gitVersionName: String by rootProject.extra
version = gitVersionName
group = project.properties["GROUP"].toString()
android {
    namespace = "com.pnuema.android.pdfviewer"
    base.archivesName.set("compose-pdf-viewer")
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            enableUnitTestCoverage = false
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(JavaVersion.VERSION_17.toString().toInt())
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.zoomable)
    implementation(libs.okhttp)
    implementation(libs.okhttp.brotli)
    implementation(libs.okhttp.logging)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("version") {
    doFirst {
        println("Version Name: $gitVersionName")
    }
}

val dokkaOutputDir = layout.buildDirectory.dir("dokka")
tasks {
    val sourcesJar by registering(Jar::class, fun Jar.() {
        archiveClassifier.set("sources")
        from(android.sourceSets.getByName("main").java.srcDirs)
    })

    val javadocJar by registering(Jar::class, fun Jar.() {
        dependsOn.add(dokkaGenerate)
        archiveClassifier.set("javadoc")
        from(android.sourceSets.getByName("main").java.srcDirs)
        from(dokkaOutputDir)
    })

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
    }

    dokka {
        moduleName.set(project.properties["POM_NAME"].toString())
        dokkaPublications.html {
            suppressInheritedMembers.set(true)
            failOnWarning.set(true)
            outputDirectory.set(dokkaOutputDir)
        }
        dokkaSourceSets.main {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl(project.properties["POM_URL"].toString())
            }
        }
        pluginsConfiguration.html {
            footerMessage.set("(c) ${project.properties["POM_DEVELOPER_NAME"].toString()}")
        }
    }

    build {
        dependsOn(dokkaGenerate)
    }

    preBuild {
        dependsOn("version")
    }
}
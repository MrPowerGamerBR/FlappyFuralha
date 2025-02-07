plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(project(":flappy-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                // Required for WebGL2 Bindings
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2025.2.2")
                implementation(npm("jszip", "3.10.1"))
                implementation("io.ktor:ktor-client-js:3.0.3")
            }
        }
    }
}
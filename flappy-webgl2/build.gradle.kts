plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(project(":flappy-common"))
                implementation("net.perfectdreams.harmony:harmony-webgl2:0.0.1")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                // Required for WebGL2 Bindings
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.10.5")
                implementation(npm("jszip", "3.10.1"))
                implementation("io.ktor:ktor-client-js:3.0.3")
            }
        }
    }
}
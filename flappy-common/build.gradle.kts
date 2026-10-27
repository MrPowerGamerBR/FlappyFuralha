plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()

    js {
        browser()
    }

    sourceSets {
         commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("net.perfectdreams.harmony:harmony-gl:0.0.1")
            }
        }
    }
}
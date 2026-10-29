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
                api(libs.harmony.gl)
            }
        }
    }
}
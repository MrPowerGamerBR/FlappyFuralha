plugins {
    kotlin("multiplatform") version "2.4.21" apply false
}

group = "net.perfectdreams.flappyfuralha"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.perfectdreams.net/")
    }
}
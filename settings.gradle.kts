plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "FlappyFuralha"

include(":flappy-common")
include(":flappy-lwjgl")
include(":flappy-webgl2")
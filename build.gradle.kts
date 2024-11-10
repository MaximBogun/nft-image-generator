plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "com.nft.generator"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.nft.generator.ImageNFTGeneratorKt") // Use the full name of your Kotlin file with `Kt` suffix if it's a standalone file
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}
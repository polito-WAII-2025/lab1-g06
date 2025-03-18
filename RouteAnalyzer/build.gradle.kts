plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.yaml:snakeyaml:2.0") //to read to .yml file
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
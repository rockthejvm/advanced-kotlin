plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":builder-lib-annotations"))
    implementation(project(":builder-lib-impl"))
    ksp(project(":builder-lib-impl"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
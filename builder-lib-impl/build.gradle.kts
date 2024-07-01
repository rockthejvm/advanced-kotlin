plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
    implementation(project(":builder-lib-annotations"))
}

tasks.test {
    useJUnitPlatform()
}
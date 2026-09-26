plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20260814")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDirs("core", "data")
    }
    sourceSets.test {
        kotlin.srcDir("tests")
    }
}

tasks.test {
    useJUnitPlatform()
}

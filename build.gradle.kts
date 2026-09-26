plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDir("core")
    }
    sourceSets.test {
        kotlin.srcDir("tests")
    }
}

tasks.test {
    useJUnitPlatform()
}

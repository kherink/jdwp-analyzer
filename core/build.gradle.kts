
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
    // The end-to-end test launches a debuggee JVM and drives it through the proxy with JDI.
    jvmArgs("--add-modules", "jdk.jdi")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

import org.gradle.api.JavaVersion

object Versions {
    // Config
    val minSdk = 21
    val compileSdk = 29
    val targetSdk = 29
    val buildTools = "29.0.3"
    val java = JavaVersion.VERSION_1_8
    val jvmTarget = "1.8"
    val kotlin = "1.3.72"
    val supportAnnotations = "28.0.0"
    val androidGradle = "4.0.0"
    val dokka = "0.10.1"
    val ktlint = "9.2.1"

    // Test
    val junit = "4.12"
    val assertk = "0.22"
    val mockito = "3.3.3"
    val mockitoKotlin = "2.2.0"

    // Instrumentation Test
    val androidTest = "1.2.0"
}

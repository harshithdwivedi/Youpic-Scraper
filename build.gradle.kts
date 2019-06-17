import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
    id("com.adarshr.test-logger") version "1.6.0"
    id("io.qameta.allure") version "2.7.0"
}

group = "io.github.christian-draeger"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    jcenter()
}

apply<TestLoggerPlugin>()
configure<TestLoggerExtension> {
    setTheme("mocha-parallel")
    slowThreshold = 6000
    showStandardStreams = true
}

dependencies {
    // use backported fluentlenium version (3.8.1) to support jdk 8
    // it contains all features from the 4.x.x version
    // see: https://github.com/FluentLenium/FluentLenium/releases/tag/v3.8.1
    val fluentleniumVersion = "3.8.1"
    val seleniumVersion = "3.141.59"
    val webdriverManagerVersion = "3.3.0"
    val browsermobVersion = "2.1.5"
    val skrapeitVersion = "0.6.0"
    val jUnitVersion = "5.4.0"
    val assertjVersion = "3.12.0"
    val awaitilityVersion = "3.1.6"
    val rerunnerVersion = "1.1.0"
    val kotlinLoggerVersion = "1.6.25"
    val julToSlf4jVersion = "1.7.26"

    implementation(kotlin("stdlib-jdk8"))

    compile(
        "com.opencsv",
        "opencsv",
        "4.1"
    )

    compile(
        group = "org.seleniumhq.selenium",
        name = "selenium-java",
        version = seleniumVersion
    )
    compile(
        group = "io.github.bonigarcia",
        name = "webdrivermanager",
        version = webdriverManagerVersion
    )
    compile(
        group = "net.lightbody.bmp",
        name = "browsermob-core",
        version = browsermobVersion
    )
    compile(
        group = "it.skrape",
        name = "skrapeit-core",
        version = skrapeitVersion
    )

    compile(
        group = "org.assertj",
        name = "assertj-core",
        version = assertjVersion
    )
    compile(
        group = "org.fluentlenium",
        name = "fluentlenium-junit-jupiter",
        version = fluentleniumVersion
    )
    compile(
        group = "org.fluentlenium",
        name = "fluentlenium-assertj",
        version = fluentleniumVersion
    )
    compile(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = jUnitVersion
    )
    compile(
        group = "io.github.artsok",
        name = "rerunner-jupiter",
        version = rerunnerVersion
    )
    compile(
        group = "org.awaitility",
        name = "awaitility-kotlin",
        version = awaitilityVersion
    )
    compile(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = kotlinLoggerVersion
    )
    compile(
        group = "org.slf4j",
        name = "jul-to-slf4j",
        version = julToSlf4jVersion
    )

}

configurations {
    all {
        exclude(module = "junit")
        exclude(module = "htmlunit-driver")
        exclude(module = "slf4j-log4j12")
        exclude(module = "slf4j-simple")
        exclude(module = "log4j")
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        parallelTestExecution()

        systemProperty("browser", System.getProperty("browser"))

        finalizedBy("allureReport")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

fun Test.parallelTestExecution() {
    val parallel = "junit.jupiter.execution.parallel"
    if (!project.hasProperty("serial")) {
        systemProperties = mapOf(
            "$parallel.enabled" to true,
            "$parallel.mode.default" to "concurrent",
            "$parallel.config.dynamic.factor" to 4
        )
    }
}

allure {
    autoconfigure = true
    version = "2.7.0"

    useJUnit5 {
        version = "2.7.0"
    }
}

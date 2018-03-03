import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmVersion by project
val jacocoVersion by project
val ktlintVersion by project
val jenkinsCoreVersion by project
val jenkinsTestHarnessVersion by project
val jenkinsCredentialsPluginVersion by project
val sezpozVersion by project
val atriumVersion by project

plugins {
    kotlin("jvm") version "1.2.20"
    kotlin("kapt") version "1.2.20"
    id("org.jenkins-ci.jpi") version "0.25.0"
    id("org.jetbrains.dokka") version "0.9.15"
    id("com.diffplug.gradle.spotless") version "3.7.0"
    jacoco
}

dependencies {

    testCompile("junit:junit:4.12")
    testCompile("ch.tutteli:atrium-cc-en_UK-robstoll:$atriumVersion")
    testCompile("ch.tutteli:atrium-verbs:$atriumVersion")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0-alpha02")

    jenkinsPlugins("org.jenkins-ci.plugins:credentials:$jenkinsCredentialsPluginVersion")
    jenkinsPlugins("org.jenkins-ci.plugins.kotlin:kotlin-v1-stdlib-jdk8:1.0-SNAPSHOT")

    jenkinsTest("org.jenkins-ci.main:jenkins-test-harness:$jenkinsTestHarnessVersion") { isTransitive = true }

    kapt("net.java.sezpoz:sezpoz:$sezpozVersion")
}

jenkinsPlugin {
    displayName = "Accurev Client Plugin"
    shortName = "accurev-client"
    gitHubUrl = "https://github.com/casz/accurev-client-plugin"
    url = "https://wiki.jenkins.io/display/JENKINS/Accurev+Client+Plugin"

    coreVersion = jenkinsCoreVersion as String
    fileExtension = "hpi"
    pluginFirstClassLoader = true
    workDir = file("$buildDir/work")

    developers = this.Developers().apply {
        developer(delegateClosureOf<JpiDeveloper> {
            setProperty("id", "casz")
            setProperty("name", "Joseph Petersen")
            setProperty("email", "josephp90@gmail.com")
            setProperty("timezone", "UTC+1")
        })
    }
    licenses = this.Licenses().apply {
        license(delegateClosureOf<JpiLicense> {
            setProperty("url", "https://jenkins.io/license/")
        })
    }
}

spotless {
    kotlin {
        // optionally takes a version
        ktlint(ktlintVersion as String)
    }
    kotlinGradle {
        target(listOf("*.gradle.kts"))
        ktlint(ktlintVersion as String)
    }
}

jacoco {
    toolVersion = jacocoVersion as String
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<JacocoReport> {
        reports {
            xml.isEnabled
        }
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = jvmVersion as String
        }
    }
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.3.1"
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    maven(url = "https://repo.jenkins-ci.org/public/")
    jcenter()
}

// Workaround for https://issues.jenkins-ci.org/browse/JENKINS-48353
configurations.all { exclude(module = "junit-dep") }

// Workaround for https://github.com/Kotlin/dokka/issues/146
buildscript {
    repositories {
        maven(url = "https://repo.jenkins-ci.org/public/")
        jcenter()
    }
}

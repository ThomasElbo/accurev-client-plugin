import org.gradle.kotlin.dsl.jenkinsPlugin
import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jenkinsci.plugins"
version = "1.0.0-SNAPSHOT"

repositories {
    maven(url = "https://repo.jenkins-ci.org/public/")
    jcenter()
}

val kotlinVersion by extra { "1.1.60" }

plugins {
    val kotlinVersion = "1.1.60"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("org.jenkins-ci.jpi") version "0.24.0"
}

jenkinsPlugin {
    coreVersion = "2.73.3"
    displayName = "Accurev Client Plugin"
    shortName = "accurev-client"
    workDir = file("$buildDir/work")

    developers = this.Developers().apply {
        developer(delegateClosureOf<JpiDeveloper> {
            setProperty("id", "casz")
            setProperty("name", "Joseph Petersen")
            setProperty("email", "josephp90@gmail.com")
        })
    }
    licenses = this.Licenses().apply {
        license(delegateClosureOf<JpiLicense> {
            setProperty("url", "https://jenkins.io/license/")
        })
    }
}

dependencies {
    compile(kotlin("stdlib-jre8", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))

    jenkinsPlugins("org.jenkins-ci.plugins:credentials:2.1.16@jar")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.3.1"
    distributionType = Wrapper.DistributionType.ALL
}

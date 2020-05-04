
import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val jvmVersion: Any? by project
val jacocoVersion: Any? by project
val ktlintVersion: Any? by project
val jenkinsCoreVersion: Any? by project
val jenkinsTestHarnessVersion: Any? by project
val jenkinsCredentialsPluginVersion: Any? by project
val sezpozVersion: Any? by project
val atriumVersion: Any? by project

plugins {
    kotlin("jvm") version "1.2.20"
    kotlin("kapt") version "1.2.20"
    id("org.jenkins-ci.jpi") version "0.28.1"
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.diffplug.gradle.spotless") version "3.7.0"
    id("com.jfrog.artifactory") version "4.9.8"
    `maven-publish`
    maven
    jacoco
    java
    id("com.github.johnrengelman.shadow") version "4.0.4"
}
val spekVersion = "1.1.5"
val junitPlatformVersion = "1.1.0"
dependencies {
    testRuntime("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("ch.tutteli:atrium-cc-en_UK-robstoll:$atriumVersion")
    testCompile("ch.tutteli:atrium-verbs:$atriumVersion")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0-alpha02")

    jenkinsPlugins("org.jenkins-ci.plugins.kotlin:kotlin-v1-stdlib-jdk8:1.2.60-1.3")
    jenkinsTest("org.jenkins-ci.main:jenkins-test-harness:$jenkinsTestHarnessVersion") { isTransitive = true }
    jenkinsPlugins(group = "org.jenkins-ci.plugins", name = "credentials", version = "2.1.18")
    jenkinsPlugins(group = "org.jenkins-ci.plugins", name = "credentials-binding", version = "1.18")
    testCompile("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release") {
        exclude(group = "com.google.guava")
    }
    kapt("net.java.sezpoz:sezpoz:$sezpozVersion")
    //compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")
    testCompile("com.palantir.docker.compose:docker-compose-rule-junit4:0.36.3-rc1")
    testCompile("com.google.guava:guava:27.0.1-jre")

    //compile("com.google.guava:guava:27.0.1-jre")
}

jenkinsPlugin {

    coreVersion = "2.74"

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

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:27.0.1-jre")
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

//artifactory {
//    setContextUrl("https://testartifactory.widex.io")
//    publish(delegateClosureOf<PublisherConfig> {
//        repository(delegateClosureOf<GroovyObject> {
//            setProperty("repoKey", "maven-playground")
//            setProperty("username", "tmel")
//            setProperty("password", "****")
//            setProperty("maven", true)
//        })
//        defaults(delegateClosureOf<GroovyObject> {
//            invokeMethod("publications", "mavenJava")
//            setProperty("publishArtifacts", true)
//            setProperty("publishPom", true)
//        })
//    })
//}

//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = "jenkins-plugins"
//            artifactId = "accurev-client"
//            version = "1.0.0-SNAPSHOT"
//
//            artifact("$buildDir/libs/accurev-client.hpi")
//            artifact("$buildDir/libs/accurev-client-plugin-1.0.0-SNAPSHOT.jar")
//
//            pom {
//                withXml {
//                }
//            }
//        }
//    }
//}

tasks {
    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            xml.destination = file("$buildDir/jacoco/test/report.xml")
        }
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = jvmVersion as String
        }
    }
    withType<Test> {
        testLogging.showStandardStreams = true
        testLogging.events("passed", "skipped", "failed")
        testLogging.setExceptionFormat("full")
    }

    withType<ShadowJar> {
        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        }
    }
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.8.1"
    distributionType = Wrapper.DistributionType.ALL
}

allprojects {
    repositories {
        maven(url = "https://repo.jenkins-ci.org/public/")
        maven(url = "https://dl.bintray.com/palantir/releases")
        maven(url = "https://mvnrepository.com/artifact/com.google.guava/guava")
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}

// Workaround for https://issues.jenkins-ci.org/browse/JENKINS-48353
configurations.all { exclude(module = "junit-dep") }

// Workaround for https://github.com/Kotlin/dokka/issues/146
buildscript {
    repositories {
        maven(url = "https://repo.jenkins-ci.org/public/")
        maven(url = "https://dl.bintray.com/palantir/releases")
        maven(url = "https://mvnrepository.com/artifact/com.google.guava/guava")
        jcenter()
    }
    dependencies {
        classpath ("com.github.jengelman.gradle.plugins:shadow:4.0.4")
    }
}

//configurations.all {
//    resolutionStrategy.force("com.google.guava:guava:27.0.1-jre" )
//}

import groovy.lang.GroovyObject
import org.jenkinsci.gradle.plugins.jpi.JpiDeveloper
import org.jenkinsci.gradle.plugins.jpi.JpiLicense
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

val sezpozVersion by project
val atriumVersion by project
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
    jacoco
	java
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

    //jenkinsPlugins("org.jenkins-ci.plugins.kotlin:kotlin-v1-stdlib-jdk8:1.0-SNAPSHOT")
    jenkinsTest("org.jenkins-ci.main:jenkins-test-harness:$jenkinsTestHarnessVersion") { isTransitive = true }
    jenkinsPlugins(group = "org.jenkins-ci.plugins", name =  "credentials", version = "2.1.18")
    jenkinsPlugins(group = "org.jenkins-ci.plugins", name = "credentials-binding", version = "1.18")
    compile("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
    kapt("net.java.sezpoz:sezpoz:$sezpozVersion")
	compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.51")

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



artifactory {
    setContextUrl("http://localhost:8081/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", "generic-local")
            setProperty("username", "admin")
            setProperty("password", "artifactory")
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "mavenJava")
            setProperty("publishArtifacts", true)
            setProperty("publishPom", true)
        })
    })
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "jenkins-plugins"
            artifactId = "accurev-client"
            version = "1.0.0-SNAPSHOT"

            artifact("$buildDir/libs/accurev-client.hpi")

            pom {
                withXml {
                  
                }
            }
        }
    }
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
    gradleVersion = "4.8.1"
    distributionType = Wrapper.DistributionType.ALL
}


repositories {
    maven(url = "https://repo.jenkins-ci.org/public/")
    jcenter()
	mavenCentral()
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

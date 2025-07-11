plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("org.sonarqube")
    id("maven-publish")
    id("net.researchgate.release")
    id("org.jreleaser")
}

val javaVersion: String by System.getProperties()
val mavenGroup: String by System.getProperties()
group = mavenGroup

dependencies {
    implementation(platform(libs.bom))

    // TEST ---------
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion))
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    withType<Wrapper> {
        val gradleReleaseVersion: String by System.getProperties()
        gradleVersion = gradleReleaseVersion
        distributionType = Wrapper.DistributionType.BIN
    }

    withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    dokkaJavadoc {
        outputDirectory.set(layout.buildDirectory.dir("documentation/javadoc"))
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaJavadoc").get().outputDirectory)
    }

    named("jreleaserFullRelease") {
        dependsOn("publish")
    }

    named("afterReleaseBuild") {
        dependsOn(
            "publishToMavenLocal",
            "jreleaserFullRelease"
        )
    }
}

sonarqube {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.projectKey", "library::kotlin-encryption")
        property("sonar.projectName", "kotlin-encryption")
        property("sonar.sources", "src/main/kotlin,src/main/resources")
        property("sonar.exclusions", "**/src/gen/**/*")
    }
}

jacoco {
    val jacocoToolVersion: String by System.getProperties()
    toolVersion = jacocoToolVersion
}

jreleaser {
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    snapshotSupported.set(true)
                    stagingRepository("${layout.buildDirectory.get()}/staging-deploy")
                }
            }
        }
    }
    release {
        github {
            tagName.set("{{projectVersion}}")
            releaseName.set("{{projectVersion}}")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("kotlin-encryption")
                description.set("encryption utilities")
                url.set("https://mbo.dev")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    url.set("https://github.com/mbogner/library-kotlin-encryption")
                    connection.set("git@github.com:mbogner/library-kotlin-encryption.git")
                    developerConnection.set("git@github.com:mbogner/library-kotlin-encryption.git")
                }
                developers {
                    developer {
                        id.set("mbo")
                        name.set("Manuel Bogner")
                        email.set("outrage_breath.0t@icloud.com")
                        organization.set("mbo.dev")
                        organizationUrl.set("https://mbo.dev")
                        timezone.set("Europe/Vienna")
                        roles.set(listOf("developer", "architect"))
                    }
                }
                organization {
                    name.set("mbo.dev")
                    url.set("https://mbo.dev")
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            url = uri("${layout.buildDirectory.get()}/staging-deploy")
        }
    }
}

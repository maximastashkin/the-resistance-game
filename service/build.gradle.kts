import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val config4k_version: String by project
val exposed_version: String by project
val flyway_version: String by project
val postgres_version: String by project
val kodein_version: String by project

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "ru.tinkoff"
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.github.config4k:config4k:$config4k_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodein_version")

    implementation(project(":game"))
    implementation(project(":shared-models"))

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("service")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ru.tinkoff.resistance.service.AppKt"))
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    build {
        dependsOn(shadowJar)
    }
}
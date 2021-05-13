val ktor_version: String by project
val logback_version: String by project
val config4k_version: String by project
val telegram_api_version: String by project

plugins {
    id("application")
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "ru.tinkoff"
version = "0.0.1"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.github.config4k:config4k:$config4k_version")

    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:$telegram_api_version")

    implementation(project(":shared-models"))

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("bot")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ru.tinkoff.resistance.bot.ApplicationKt"))
        }
    }
}
application {
    mainClassName = "ru.tinkoff.resistance.bot.ApplicationKt"
}
tasks {
    build {
        dependsOn(shadowJar)
    }
}
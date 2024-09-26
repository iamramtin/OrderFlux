val kotlinVersion: String by project
val ktorVersion: String by project
val vertxVersion: String by project
val logbackVersion: String by project
val junitJupiterVersion: String by project

plugins {
    application
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "3.0.0-rc-1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

group = "org.iamramtin.orderflux"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Ktor dependencies
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Vert.x dependencies
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-web-client")
    implementation("io.vertx:vertx-web-validation")
    implementation("io.vertx:vertx-web-proxy")
    implementation("io.vertx:vertx-web-openapi")
    implementation("io.vertx:vertx-web-openapi-router")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("io.vertx:vertx-auth-common")
    implementation("io.vertx:vertx-auth-jwt")

    // Other dependencies
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // Test dependencies
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("io.ktor:ktor-client-mock")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

application {
    mainClass.set("org.iamramtin.MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.iamramtin.MainKt"
    }

    // Include the compiled kotlin classes and dependencies in the JAR
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    // Exclude META-INF/*.SF and META-INF/*.DSA files from the JAR
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // Resolve conflicts between files with the same path by excluding the duplicates
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}
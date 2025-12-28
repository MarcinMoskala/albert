import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Properties
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.jib)
}

group = "com.marcinmoskala.albert"
version = "1.0.0"

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val isProductionBuild: Boolean = providers.gradleProperty("production").isPresent
val includeComposeStaticResources: Boolean =
    !isProductionBuild && !project.hasProperty("skipComposeStatic") && project.findProject(":composeApp") != null

fun loadLocalPropertiesFromRootProject(): Properties {
    val loadedProperties = Properties()
    val propertiesFile = rootProject.file("local.properties")
    if (!propertiesFile.exists()) {
        return loadedProperties
    }

    propertiesFile.inputStream().use { loadedProperties.load(it) }
    return loadedProperties
}

application {
    mainClass.set("com.marcinmoskala.albert.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}
// Railway / container friendly: expose port via PORT env var (default handled in Application.kt)

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kaml)
    implementation(libs.koin.ktor)
    implementation(libs.koin.loggerSlf4j)
    implementation(libs.jwt)
    implementation(libs.firebase.admin)
    implementation("io.ktor:ktor-server-partial-content-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.clientContentNegotiation)
    testImplementation(libs.kotlin.testJunit)
}

// Container image configuration using Google Jib
jib {
    from {
        image = "eclipse-temurin:21-jre"
    }
    to {
        image = "albert-server:latest"
    }
    container {
        mainClass = "com.marcinmoskala.albert.ApplicationKt"
        jvmFlags = listOf("-Dio.ktor.development=true")
        ports = listOf("8080")
    }
}

tasks.named<ProcessResources>("processResources") {
    if (includeComposeStaticResources) {
        // For local backend runs we want a build that always terminates.
        // Production webpack uses Terser minification which can stall on Windows for large bundles.
        dependsOn(":composeApp:jsBrowserDevelopmentExecutableDistribution")

        val composeAppBuildDirectory = project(":composeApp").layout.buildDirectory
        from(composeAppBuildDirectory.dir("dist/js/developmentExecutable")) {
            into("static")
        }
    } else {
        logger.lifecycle("processResources: skipping compose web bundle (production/skipComposeStatic)")
    }
}

// Write Firebase credentials to a temporary file for IntelliJ run configurations
val writeFirebaseCredentials by tasks.registering {
    // This task writes a local file and depends on values outside of Gradle inputs (local.properties).
    // Make it opt-out of the configuration cache so it doesn't fail builds when CC is enabled.
    notCompatibleWithConfigurationCache(
        "Writes Firebase credentials from local.properties to a temp file for local development"
    )

    doLast {
        val loadedLocalProperties = loadLocalPropertiesFromRootProject()
        val serviceAccountJson = loadedLocalProperties.getProperty("firebase.service.account.json")
        if (serviceAccountJson != null) {
            val tempDir = file("${layout.buildDirectory.get()}/firebase")
            tempDir.mkdirs()
            val credFile = file("${tempDir}/service-account.json")
            credFile.writeText(serviceAccountJson)
            println("Firebase credentials written to: ${credFile.absolutePath}")
        }
    }
}

tasks.named<JavaExec>("run") {
    dependsOn(writeFirebaseCredentials)

    // Load all properties from local.properties and set as environment variables
    localProperties.forEach { key, value ->
        val keyStr = key.toString()
        // Skip Android SDK path and convert property names to env var format
        if (keyStr != "sdk.dir") {
            // Convert property name to UPPER_SNAKE_CASE environment variable
            val envVarName = keyStr.replace(".", "_").uppercase()
            environment(envVarName, value.toString())
        }
    }

    // Also set path to credentials file for fallback
    val tempDir = file("${layout.buildDirectory.get()}/firebase")
    val credFile = file("${tempDir}/service-account.json")
    if (credFile.exists()) {
        environment("GOOGLE_APPLICATION_CREDENTIALS", credFile.absolutePath)
    }
}

// For IntelliJ IDEA run configurations
tasks.withType<JavaExec>().configureEach {
    // Set environment variables for all JavaExec tasks
    if (name.contains("main", ignoreCase = true)) {
        doFirst {
            localProperties.forEach { key, value ->
                val keyStr = key.toString()
                if (keyStr != "sdk.dir") {
                    val envVarName = keyStr.replace(".", "_").uppercase()
                    environment(envVarName, value.toString())
                }
            }
        }
        // Configuration cache does not support the captured script object references in these JavaExec tasks
        // (env injection from local.properties). Mark them as incompatible so the rest of the build can still be cached.
        notCompatibleWithConfigurationCache(
            "JavaExec tasks inject environment variables from local.properties via script closures"
        )
    }
}

tasks.register<Exec>("dockerRun") {
    group = "docker"
    description = "Build image with Jib and run the backend container (maps 8080:8080)"
    dependsOn("jibDockerBuild")
    dependsOn(writeFirebaseCredentials)

    doFirst {
        val environmentFile = file("${layout.buildDirectory.get()}/docker/env.list")
        environmentFile.parentFile.mkdirs()

        val environmentFileContents = buildString {
            localProperties.forEach { key, value ->
                val keyStr = key.toString()
                if (keyStr == "sdk.dir") return@forEach
                if (keyStr == "firebase.service.account.json") return@forEach

                val environmentVariableName = keyStr.replace(".", "_").uppercase()
                append(environmentVariableName)
                append('=')
                append(value.toString())
                append('\n')
            }
        }
        environmentFile.writeText(environmentFileContents)

        val tempDir = file("${layout.buildDirectory.get()}/firebase")
        val credentialsFile = file("${tempDir}/service-account.json")

        val dockerCommandLine = mutableListOf(
            "docker",
            "run",
            "--rm",
            "-p",
            "8080:8080",
            "--env-file",
            environmentFile.absolutePath,
        )

        if (credentialsFile.exists()) {
            dockerCommandLine.addAll(
                listOf(
                    "-e",
                    "GOOGLE_APPLICATION_CREDENTIALS=/app/firebase/service-account.json",
                    "-v",
                    "${credentialsFile.absolutePath}:/app/firebase/service-account.json:ro",
                )
            )
        }

        dockerCommandLine.add("albert-server:latest")
        commandLine(dockerCommandLine)
    }
}

val serverPidFileProvider = layout.buildDirectory.file("server/server.pid")
val serverLogFileProvider = layout.buildDirectory.file("server/server.log")

fun MutableMap<String, String>.putEnvironmentVariablesFromLocalProperties(loadedLocalProperties: Properties) {
    loadedLocalProperties.forEach { key, value ->
        val keyStr = key.toString()
        if (keyStr != "sdk.dir") {
            val envVarName = keyStr.replace(".", "_").uppercase()
            put(envVarName, value.toString())
        }
    }

    val credentialsFile = file("${layout.buildDirectory.get()}/firebase/service-account.json")
    if (credentialsFile.exists()) {
        put("GOOGLE_APPLICATION_CREDENTIALS", credentialsFile.absolutePath)
    }
}

fun isProcessAlive(processId: Long): Boolean {
    val processHandle = ProcessHandle.of(processId).orElse(null)
    return processHandle?.isAlive == true
}

fun isLocalTcpPortInUse(port: Int): Boolean {
    if (port !in 1..65535) return false
    return try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress("127.0.0.1", port), 250)
        }
        true
    } catch (_: Exception) {
        false
    }
}

tasks.register("startServer") {
    group = "application"
    description =
        "Starts the Ktor backend in the background (writes PID + logs under server/build/server/)"

    dependsOn("classes")
    dependsOn("processResources")
    dependsOn(writeFirebaseCredentials)

    notCompatibleWithConfigurationCache(
        "Starts a long-lived background process and reads local.properties at execution time"
    )

    doLast {
        val pidFile = serverPidFileProvider.get().asFile
        val logFile = serverLogFileProvider.get().asFile

        val existingProcessId = pidFile
            .takeIf { it.exists() }
            ?.readText()
            ?.trim()
            ?.toLongOrNull()

        if (existingProcessId != null && isProcessAlive(existingProcessId)) {
            println("Backend already running (pid=$existingProcessId). Logs: ${logFile.absolutePath}")
            return@doLast
        }

        pidFile.parentFile.mkdirs()
        logFile.parentFile.mkdirs()

        val javaBinDirectory = file("${System.getProperty("java.home")}/bin")
        val javaExecutableFile = javaBinDirectory
            .resolve("java.exe")
            .takeIf { it.exists() }
            ?: javaBinDirectory.resolve("java")

        val mainRuntimeClasspathFiles =
            project.the<SourceSetContainer>()["main"].runtimeClasspath.files
        val classpath = mainRuntimeClasspathFiles
            .joinToString(separator = File.pathSeparator) { it.absolutePath }

        val serverPort =
            providers.gradleProperty("serverPort").orNull?.toIntOrNull()
                ?: 8080

        if (isLocalTcpPortInUse(serverPort)) {
            error(
                "Port $serverPort is already in use, so the backend would immediately exit. " +
                        "Stop the other process or run with -PserverPort=XXXX."
            )
        }

        val serverCommand = listOf(
            javaExecutableFile.absolutePath,
            "-Dio.ktor.development=true",
            "-Dserver.port=$serverPort",
            "-cp",
            classpath,
            "com.marcinmoskala.albert.ApplicationKt",
        )

        val loadedLocalProperties = loadLocalPropertiesFromRootProject()
        val environmentVariables = mutableMapOf<String, String>()
        environmentVariables.putEnvironmentVariablesFromLocalProperties(loadedLocalProperties)

        val processBuilder = ProcessBuilder(serverCommand)
            .directory(project.projectDir)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))

        processBuilder.environment().putAll(environmentVariables)

        val serverProcess = processBuilder.start()
        pidFile.writeText(serverProcess.pid().toString())

        println("Backend started (pid=${serverProcess.pid()}).")
        println("Port: $serverPort")
        println("Logs: ${logFile.absolutePath}")
        println("Stop with: ./gradlew :server:stopServer")
    }
}

tasks.register("stopServer") {
    group = "application"
    description = "Stops the background Ktor backend previously started with :server:startServer"

    notCompatibleWithConfigurationCache(
        "Stops a long-lived background process using a PID file"
    )

    doLast {
        val pidFile = serverPidFileProvider.get().asFile
        if (!pidFile.exists()) {
            println("No PID file found at ${pidFile.absolutePath}. Is the backend running?")
            return@doLast
        }

        val processId = pidFile
            .readText()
            .trim()
            .toLongOrNull()

        if (processId == null) {
            println("PID file did not contain a valid process id. Deleting ${pidFile.absolutePath}.")
            pidFile.delete()
            return@doLast
        }

        val processHandle = ProcessHandle.of(processId).orElse(null)
        if (processHandle == null || !processHandle.isAlive) {
            println("Backend is not running (pid=$processId). Cleaning up PID file.")
            pidFile.delete()
            return@doLast
        }

        processHandle.destroy()

        val waitDeadlineMillis = System.currentTimeMillis() + 5_000
        while (processHandle.isAlive && System.currentTimeMillis() < waitDeadlineMillis) {
            Thread.sleep(100)
        }

        if (processHandle.isAlive) {
            processHandle.destroyForcibly()
        }

        pidFile.delete()
        println("Backend stopped (pid=$processId).")
    }
}
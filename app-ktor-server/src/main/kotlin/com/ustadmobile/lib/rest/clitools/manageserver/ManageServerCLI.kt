package com.ustadmobile.lib.rest.clitools.manageserver

import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.delete.DeleteLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.update.UpdateLearningSpaceUseCase
import com.ustadmobile.lib.rest.ext.ktorAppHomeDir
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.sourceforge.argparse4j.inf.FeatureControl
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparsers
import java.io.File
import kotlin.system.exitProcess

internal fun Subparsers.addNewLearningSpaceParser() {
    addParser("newlearningspace").also {
        it.help("Add a new learning space")
        it.addArgument("-s", "--server")
            .setDefault("http://localhost:8087/")
            .help(FeatureControl.SUPPRESS)

        it.addArgument("-t", "--title")
            .required(true)
            .help("Learning space title")
        it.addArgument("-u", "--url")
            .required(true)
            .help("Learning space url eg. https://schoolname.examples.org/. " +
                    "This must match the url as a user would enter it into their browser.")
        it.addArgument("-d", "--dburl")
            .help("Learning space database JDBC URL")
        it.addArgument("-n", "--dbusername")
            .setDefault("")
            .help("Learning space database username")
        it.addArgument("-w", "--dbpassword")
            .setDefault("")
            .help("Learning space database admin user e.g. admin")
        it.addArgument("-a", "--adminuser")
            .setDefault("admin")
            .help("Learning space initial admin username e.g. admin")

    }
}


internal fun Subparsers.addUpdateLearningSpaceSubcommand() {
    addParser("updatelearningspace").also {
        it.help("Update an existing learning space")
        it.addArgument("-t", "--title")
            .required(true)
            .help("Learning Space title")
        it.addArgument("-u", "--url")
            .required(true)
            .help("Learning Space url")
        it.addArgument("-d", "--dburl")
            .help("Database JDBC URL")
        it.addArgument("-n", "--dbusername")
            .setDefault("")
            .help("Database username")
        it.addArgument("-w", "--dbpassword")
            .setDefault("")
            .help("Database password")
        it.addArgument("-a", "--adminuser")
            .setDefault("admin")
            .help("Username for learning space admin")


    }
}

internal fun Subparsers.addDeleteLearningSpaceSubcommand() {
    addParser("deletelearningspace").also {
        it.help("Delete an existing learning space")
        it.addArgument("-u", "--url")
            .required(true)
            .help("Learning Space url")
        it.addArgument("-a", "--adminuser")
            .setDefault("admin")
            .help("Username for learning space admin")


    }
}


fun main(ns: Namespace) {
    val json = Json { encodeDefaults = true }
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json = json)
        }
    }
    val dataDir = File("${ktorAppHomeDir().absolutePath}/data")
    val siteFile = File(dataDir, "server.properties")
    val adminPassword = File(dataDir, "admin.txt").readText().trim()
    val dbUrl = ns.getString("dburl") ?:
    "jdbc:sqlite:$dataDir/${sanitizeDbNameFromUrl(ns.getString("url"))}.db"

    if (!dataDir.exists() || !siteFile.exists()) {
        println("Error: Server is not running.")
        exitProcess(1)
    }

    try {
        val serverUrl = ns.getString("server")

        runBlocking {

            when(ns.getString("subparser_name")) {
                "newlearningspace" -> {
                    val request = CreateLearningSpaceUseCase.CreateLearningSpaceRequest(
                        url = ns.getString("url"),
                        title = ns.getString("title"),
                        dbUrl = dbUrl,
                        dbUsername = ns.getString("dbusername"),
                        dbPassword = ns.getString("dbpassword"),
                        adminUsername = ns.getString("adminuser"),
                        adminPassword = adminPassword
                    )
                    println(request)

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/create") {
                        headers["Authorization"] = "Basic ${("admin:$adminPassword").encodeBase64()}"

                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    val responseText = response.bodyAsText()
                    println("Done: $responseText")
                }
                "deletelearningspace" -> {
                    val learningSpaceUrl = ns.getString("url")

                    println("Are you sure you want to delete the learning space at URL: $learningSpaceUrl? (yes/no)")

                    val userConfirmation = readLine()?.lowercase()
                    if (userConfirmation != "yes") {
                        println("Deletion cancelled.")
                        return@runBlocking
                    }

                    val request = DeleteLearningSpaceUseCase.DeleteLearningSpaceUseCase(
                        url = learningSpaceUrl,
                    )

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/delete") {
                        headers["Authorization"] = "Basic ${("admin:$adminPassword").encodeBase64()}"

                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    val responseText = response.bodyAsText()
                    println("Done: $responseText")
                }
                "updatelearningspace" -> {
                    val request = UpdateLearningSpaceUseCase.UpdateLearningSpaceUseCase(
                        url = ns.getString("url"),
                        title = ns.getString("title") ,
                        dbUrl = dbUrl,
                        dbUsername = ns.getString("dbusername"),
                        dbPassword = ns.getString("dbpassword"),
                        adminUsername = ns.getString("adminuser"),
                        adminPassword = adminPassword
                    )

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/update") {
                        headers["Authorization"] = "Basic ${("admin:$adminPassword").encodeBase64()}"

                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    val responseText = response.bodyAsText()
                    println("Done: $responseText")
                }
                else -> {
                    println("No such command")
                }
            }
        }
    }catch(e: Throwable) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
        exitProcess(2)
    }finally {
        httpClient.close()
    }
}
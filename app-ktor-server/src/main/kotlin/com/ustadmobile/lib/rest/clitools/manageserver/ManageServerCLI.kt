package com.ustadmobile.lib.rest.clitools.manageserver

import com.ustadmobile.lib.rest.SERVER_PROPERTIES_KEY_PORT
import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.delete.DeleteLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.update.UpdateLearningSpaceUseCase
import com.ustadmobile.lib.rest.ext.ktorAppHomeDir
import com.ustadmobile.lib.rest.ext.ktorServerPropertiesFile
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
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.FeatureControl
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparsers
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess

internal fun Subparsers.addNewLearningSpaceParser() {
    addParser("newlearningspace").also {
        it.help("Add a new learning space")
        it.addArgument("-s", "--server")
            .setDefault("http://localhost:8087/")
            .help(FeatureControl.SUPPRESS)

        it.addArgument("--isSelfRegistered")
            .action(Arguments.storeTrue())
            .help("Flag to indicate if the learning space allows self-registration")

        it.addArgument("-t", "--title")
            .required(true)
            .help("Learning space title")
        it.addArgument("-u", "--url")
            .required(true)
            .help("The URL for users to access the learning space via \n" +
                    "the browser or mobile/desktop apps. If you are using a reverse proxy (as recommended)" +
                    "this URL must be the URL as it would be entered by the user, not the ProxyPass parameter.")

        it.addArgument("--subdomain")
            .help("Custom subdomain for the learning space")

        it.addArgument("-c", "--admincontact")
            .required(true)
            .help("Admin contact email or phone number")

        it.addArgument("-l", "--organisationlogo")
            .help("URL of the organisation's logo")

        it.addArgument("-d", "--dburl")
            .help("Learning space database JDBC URL")
        it.addArgument("-n", "--dbusername")
            .setDefault("")
            .help("Learning space database username")
        it.addArgument("-w", "--dbpassword")
            .setDefault("")
            .help("Learning space database admin user e.g. admin")
        it.addArgument("-a", "--adminusername")
            .setDefault("admin")
            .help("Learning space initial admin username (default admin)")
        it.addArgument("-p", "--adminpassword")
            .help("Learning space initial admin password (the default username for the initial admin " +
                    "user will be admin unless set otherwise.")
            .required(true)
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
    val serverPropertiesFile = ktorServerPropertiesFile()

    if(!serverPropertiesFile.exists()) {
        println("Error: Server is not running: server.properties does not exist")
        exitProcess(1)
    }

    val serverProperties = Properties()
    serverPropertiesFile.reader().use { serverPropertiesReader ->
        serverProperties.load(serverPropertiesReader)
    }

    val port = serverProperties.getProperty(SERVER_PROPERTIES_KEY_PORT)

    val systemConfigAuth = File(dataDir, "admin.txt").readText().trim()
    val dbUrl = ns.getString("dburl")

    if (!dataDir.exists() || !serverPropertiesFile.exists()) {
        println("Error: Server is not running.")
        exitProcess(1)
    }

    try {
        runBlocking {
            val serverUrl = "http://localhost:$port/"

            when(ns.getString("subparser_name")) {
                "newlearningspace" -> {
                    val request = CreateLearningSpaceUseCase.CreateLearningSpaceRequest(
                        url = ns.getString("url"),
                        title = ns.getString("title"),
                        dbUrl = ns.getString("dburl"),
                        dbUsername = ns.getString("dbusername"),
                        dbPassword = ns.getString("dbpassword"),
                        adminUsername = ns.getString("adminusername"),
                        adminPassword = ns.getString("adminpassword"),
                        isSelfRegistered = ns.getBoolean("isSelfRegistered"),
                        subdomain = ns.getString("subdomain"),
                        adminContact = ns.getString("admincontact"),
                        organisationLogo = ns.getString("organisationlogo")
                    )
                    println(request)

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/create") {
                        headers["Authorization"] = "Basic ${("admin:$systemConfigAuth").encodeBase64()}"

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
                        headers["Authorization"] = "Basic ${("admin:$systemConfigAuth").encodeBase64()}"

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
                        adminUsername = ns.getString("adminusername"),
                        adminPassword = systemConfigAuth
                    )

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/update") {
                        headers["Authorization"] = "Basic ${("admin:$systemConfigAuth").encodeBase64()}"

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
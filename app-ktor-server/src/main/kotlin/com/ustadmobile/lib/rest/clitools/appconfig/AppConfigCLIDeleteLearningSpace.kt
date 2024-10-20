package com.ustadmobile.lib.rest.clitools.appconfig

import com.ustadmobile.lib.rest.domain.learningspace.delete.DeleteLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.update.UpdateLearningSpaceUseCase
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
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("AppConfig").build()
        .defaultHelp(true)
        .description("Ustad AppConfig Manager CLI")

    val json = Json { encodeDefaults = true }
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json = json)
        }
    }

    parser.addArgument("-s", "--server")
        .setDefault("http://localhost:8087/")
    parser.addArgument("-a", "--password")
        .required(true)
        .help("System admin password")

    parser.addSubparsers().also { subParsers ->
        subParsers.title("subcommands")
        subParsers.dest("subparser_name")
        subParsers.addParser("deletelearningspace").also {
            it.addArgument("-u", "--url")
                .required(true)
                .help("Learning Space url")
            it.addArgument("-a", "--adminuser")
                .setDefault("admin")
                .help("Username for learning space admin")
            it.addArgument("-p", "--adminpassword")
                .required(true)
                .help("Initial password for learning space admin")

        }
    }

    val ns: Namespace
    try {
        ns = parser.parseArgs(args)
        val serverUrl = ns.getString("server")
        val adminPassword = ns.getString("password")

        runBlocking {
            when(ns.getString("subparser_name")) {
                "deletelearningspace" -> {
                    val request = DeleteLearningSpaceUseCase.DeleteLearningSpaceUseCase(
                        url = ns.getString("url"),
                    )

                    val response = httpClient.post("${serverUrl}config/api/learningspaces/delete") {
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
    }catch(e: ArgumentParserException) {
        parser.handleError(e)
        exitProcess(1)
    }catch(e: Throwable) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
        exitProcess(2)
    }finally {
        httpClient.close()
    }
}
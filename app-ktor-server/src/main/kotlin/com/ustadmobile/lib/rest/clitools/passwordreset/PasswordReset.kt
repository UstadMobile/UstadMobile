package com.ustadmobile.lib.rest.clitools.passwordreset

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.rest.CONF_DBMODE_SINGLETON
import com.ustadmobile.lib.rest.dimodules.makeJvmBackendDiModule
import com.ustadmobile.lib.rest.ext.dbModeProperty
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.kodein.di.*
import kotlin.system.exitProcess

/**
 * Basic command line password reset implementation
 */
fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("PasswordReset").build()
        .defaultHelp(true)
        .description("Ustad server command line password reset")

    parser.addArgument("-c", "--config")
        .setDefault("ustad-server.conf")
        .help("Server config file path")

    parser.addArgument("-e", "--endpoint")
        .help("Server endpoint url e.g. https://ustad.servername.com/")
    parser.addArgument("-u", "--username")
        .required(true)
        .help("Username to reset password for")

    val ns: Namespace
    try {
        ns = parser.parseArgs(args)
    }catch(e: ArgumentParserException) {
        parser.handleError(e)
        exitProcess(1)
    }

    println("Loading ${ns.getString("config")}")

    try {
        ApplicationConfig(ns.getString("config"))
    }catch(e: Exception) {
        e.printStackTrace()
    }
    val conf = ApplicationConfig(ns.getString("config"))

    println("Loaded config")

    val endpoint = if(conf.dbModeProperty() == CONF_DBMODE_SINGLETON) {
        Endpoint("http://localhost/")
    }else {
        val endpointArg = ns.getString("endpoint")
        if(endpointArg == null){
            println("ERROR: Configuration uses virtual hosting: but no endpoint specified")
            exitProcess(2)
        }
        Endpoint(endpointArg)
    }

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val di = DI {
        import(makeJvmBackendDiModule(conf, json = json))
    }


    val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
    val person = db.personDao.findByUsername(ns.getString("username"))

    if(person != null) {
        val authManager: AuthManager = di.direct.on(endpoint).instance()
        print("Please enter new password: ")
        val newPassword = readln().trim()
        runBlocking {
            authManager.setAuth(person.personUid, newPassword)
        }
    }else {
        println("ERROR: user not found")
    }

}
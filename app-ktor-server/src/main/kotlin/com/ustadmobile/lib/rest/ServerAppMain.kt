package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.clitools.manageserver.addDeleteLearningSpaceSubcommand
import com.ustadmobile.lib.rest.clitools.manageserver.addNewLearningSpaceParser
import com.ustadmobile.lib.rest.clitools.manageserver.addUpdateLearningSpaceSubcommand
import com.ustadmobile.lib.rest.ext.absoluteDataDir
import com.ustadmobile.lib.rest.ext.ktorServerPropertiesFile
import com.ustadmobile.lib.rest.mediahelpers.MissingMediaProgramsException
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.netty.handler.codec.http.HttpServerCodec
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import com.ustadmobile.lib.rest.clitools.manageserver.main as manageServerMain


/**
 * ServerAppMain provides the command line entry point
 */
class ServerAppMain {

    companion object {

        const val MAX_INITIAL_LINE_LENGTH = 32 * 1024

        const val MAX_HEADER_SIZE = 4096

        const val MAX_CHUNK_SIZE = 4096

        const val CMD_RUN_SERVER = "runserver"

        const val DEFAULT_CONFIG_FILE_NAME = "ustad-server.conf"

        private fun Array<String>.argsAfterFirst(): Array<String> {
            return if(isNotEmpty())
                toList().subList(1, size).toTypedArray()
            else
                this
        }

        private fun runServerMain(args: Array<String>) {
            try {
                embeddedServer(Netty, commandLineEnvironment(args)) {
                    //Increase these timeouts to allow for ServerSentEvents which keep the client waiting
                    requestReadTimeoutSeconds = 600
                    responseWriteTimeoutSeconds = 600
                    httpServerCodec = {
                        HttpServerCodec(MAX_INITIAL_LINE_LENGTH, MAX_HEADER_SIZE, MAX_CHUNK_SIZE)
                    }
                }.also {
                    it.addShutdownHook {
                        ktorServerPropertiesFile(
                            dataDir = it.environment.config.absoluteDataDir()
                        ).delete()
                    }
                }.start(true)
            } catch (e: SiteConfigException) {
                System.err.println(e.message)
            } catch (e: MissingMediaProgramsException) {
                System.err.println("Required media programs (e.g. MediaInfo, Handbrake CLI, Sox, etc.) were not found.")
                System.err.println(e.message ?: "")
                System.err.println("See the README for more information")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgumentParsers.newFor("ustad-server").build()
            val subparsers = parser.addSubparsers()
                .title("subcommands")
                .description("valid subcommands")
                .dest("subparser_name")
                .help("additional help")
                .metavar("COMMAND")
            subparsers.addParser(CMD_RUN_SERVER).help("Run the Ustad HTTP server")
            subparsers.addNewLearningSpaceParser()
            subparsers.addUpdateLearningSpaceSubcommand()
            subparsers.addDeleteLearningSpaceSubcommand()

            val ns: Namespace?
            try {
                ns = parser.takeIf {
                    args.isNotEmpty() && args.firstOrNull() != CMD_RUN_SERVER
                }?.parseArgs(args)
                val subCommand = ns?.getString("subparser_name") ?: CMD_RUN_SERVER
                val configSysProp = System.getProperty("app_config")

                val configArgs = when {
                    //When config argument is explicitly set, leave as-is
                    (args.any { it.startsWith("-c=") || it.startsWith("-config=")}) -> emptyArray()

                    /*
                     * The application script templates (in src/scripttemplates) will set the default
                     * path to the KTOR (Hocon) configuration file using the app_config system property.
                     *
                     * When this is set it should be passed to the KTOR embedded server as if it was
                     * added as a command line argument.
                     */
                    configSysProp != null -> arrayOf("-config=$configSysProp")

                    //Else (e.g. if running from source) and default config file name exists, then use it
                    File(DEFAULT_CONFIG_FILE_NAME).exists() -> {
                        arrayOf("-config=$DEFAULT_CONFIG_FILE_NAME")
                    }

                    File("app-ktor-server", DEFAULT_CONFIG_FILE_NAME).exists() -> {
                        arrayOf("-config=app-ktor-server/$DEFAULT_CONFIG_FILE_NAME")
                    }

                    else -> emptyArray()
                }

                when {
                    subCommand == CMD_RUN_SERVER -> {
                        runServerMain(args.argsAfterFirst() + configArgs)
                    }

                    else -> {
                        manageServerMain(
                            ns ?: throw IllegalStateException("if args were empty would have run server")
                        )
                    }
                }
            } catch (e: ArgumentParserException) {
                parser.handleError(e)
                System.exit(if(e is HelpScreenException) 0 else 1)
            }
        }
    }

}
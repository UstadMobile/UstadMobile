package com.ustadmobile.lib.rest.clitools.servermanager

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("ServerManager").build()
        .defaultHelp(true)
        .description("Ustad server command line server manager")

    parser.addSubparsers().also { subParsers ->
        subParsers.title("subcommands")
        subParsers.dest("subparser_name")
        subParsers.addParser("newlearningspace").also {
            it.addArgument("-t", "--title")
                .help("Learning Space title")
            it.addArgument("-u", "--url")
                .help("Learning Space url")
        }
    }

    val ns: Namespace
    try {
        ns = parser.parseArgs(args)

        when(ns.getString("subparser_name")) {
            "newlearningspace" -> {
                println("New Learning Space")
            }
        }
    }catch(e: ArgumentParserException) {
        parser.handleError(e)
        exitProcess(1)
    }




}
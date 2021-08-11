package com.ustadmobile.lib.rest.torrent

import bt.Bt
import bt.data.file.FileSystemStorage
import bt.runtime.BtRuntime
import bt.torrent.fileselector.FilePriority
import bt.torrent.fileselector.FilePrioritySkipSelector
import bt.torrent.fileselector.TorrentFileSelector
import bt.torrent.maker.TorrentBuilder
import io.ktor.utils.io.concurrent.*
import org.apache.commons.cli.*
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.system.exitProcess

class Creator(file: File) {

    init{


        val sharedRuntime: BtRuntime = BtRuntime.defaultRuntime()
        sharedRuntime.startup()

        val saveDir = file.parentFile.toPath()
        val result = TorrentBuilder()
                .rootPath(saveDir)
                .addFile(file.toPath())
             //   .add(*fileList.map { it.toPath() }.toTypedArray())
                .createdBy("UstadMobile")
                .creationDate(Date())
                .announce("http://192.168.1.118:8000/announce")
                .build()

        val tempDir = Files.createTempDirectory("torrentDir").toFile()
        val torrentFile = File(saveDir.toFile(), "mytorrent.torrent")
        torrentFile.writeBytes(result)


        val storage = FileSystemStorage(saveDir)
        val client = Bt.client(sharedRuntime)
                .storage(storage)
                .torrent(torrentFile.toURI().toURL())
                .build()

        sharedRuntime.attachClient(client)
        client.startAsync().join()

    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            val fileOption = Option.builder("file")
                    .argName("file")
                    .hasArg()
                    .required()
                    .desc("file location")
                    .build()
            options.addOption(fileOption)


            val saveDirOption = Option.builder("saveDir")
                    .argName("saveDir")
                    .hasArg()
                    .desc("dir to save torrent, default same location as file")
                    .build()
            options.addOption(saveDirOption)

            val cmd: CommandLine
            try {

                val parser: CommandLineParser = DefaultParser()
                cmd = parser.parse(options, args)

            } catch (e: ParseException) {
                System.err.println("Parsing failed.  Reason: " + e.message)
                exitProcess(1)
            }

            val fileLocation = File(cmd.getOptionValue("file"))
            val saveDir = File(cmd.getOptionValue("torrentSaveDir") ?: fileLocation.parent)

            Creator(fileLocation)

        }

    }
}
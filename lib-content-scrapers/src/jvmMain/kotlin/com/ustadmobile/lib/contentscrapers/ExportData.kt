package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.openInputStream
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.getFileNameFromUrl
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.cli.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import kotlin.system.exitProcess


class ExportData(private val containerUid: Long, val path: String) {

    var db: UmAppDatabase = TODO("UmAppDatabase.getInstance(Any(), replaceMeWithDi())")

    init {
        val destDir = File(path)
        destDir.mkdirs()
        val container = db.containerDao.findByUid(containerUid) ?: exitProcess(1)

        val list = db.containerEntryDao.findByContainer(containerUid)

        list.forEach{

            try {

                val entry =  db.containerEntryDao.findByPathInContainer(containerUid, it.cePath!!)
                val input = entry!!.containerEntryFile!!.openInputStream()
                val file = File(destDir, getFileNameFromUrl(URL(entry.cePath)))
                FileUtils.copyInputStreamToFile(input, file)
            }catch(e: Exception) {

            }

        }

    }



    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            val containerOption = Option.builder("container")
                    .argName("uid")
                    .hasArg()
                    .required()
                    .desc("container uid")
                    .build()
            options.addOption(containerOption)

            val dirOption = Option.builder("dir")
                    .argName("path")
                    .hasArg()
                    .required()
                    .desc("directory to save")
                    .build()
            options.addOption(dirOption)

            val cmd: CommandLine
            try {

                val parser: CommandLineParser = DefaultParser()
                cmd = parser.parse(options, args)

            } catch (e: ParseException) {
                System.err.println("Parsing failed.  Reason: " + e.message)
                exitProcess(1)
            }

            ExportData(cmd.getOptionValue("container").toLong(), cmd.getOptionValue("dir"))


        }



    }

}
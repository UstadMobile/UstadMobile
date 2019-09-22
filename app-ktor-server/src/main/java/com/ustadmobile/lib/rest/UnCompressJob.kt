package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class UnCompressJob {

    var context = Any()

    fun startJob() {

        var db = UmAppDatabase.getInstance(context)
        var data: List<ContainerEntryFile> = db.containerEntryFileDao.getAllFilesToFixUnCompressLength()
        println("size of list: ${data.size}")
        data.forEach {

            var compresssedFile = File(it.cefPath!!)
            println("compressed Size ${compresssedFile.length()}")
            var contentIn = GZIPInputStream(FileInputStream(compresssedFile))
            var bout = ByteArrayOutputStream()
            IOUtils.copy(contentIn, bout)
            bout.flush()
            contentIn.close()
            var totalSize = bout.toByteArray().size
            println("Total Size: $totalSize")
            db.containerEntryFileDao.updateTotalLength(totalSize.toLong(), it.cefUid)
            bout.close()

        }


    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            UnCompressJob().startJob()
        }

    }

}
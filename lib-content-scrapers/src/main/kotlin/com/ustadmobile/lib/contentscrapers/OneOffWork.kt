package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.port.sharedse.util.Base64Coder
import com.ustadmobile.port.sharedse.util.UmFileUtilSe

import java.io.File
import java.io.IOException

class OneOffWork @Throws(IOException::class)
constructor() {

    init {


        val db = UmAppDatabase.getInstance(Any())
        val entryFileDao = db.containerEntryFileDao
        val containerEntryFile = entryFileDao.findByUid(500332)

        if (!containerEntryFile!!.cefPath!!.contains("500332")) {
            throw IllegalArgumentException("not the correct file")
        }

        val file = File(containerEntryFile.cefPath!!)


        val buf = ByteArray(8 * 1024)

        val md5 = Base64Coder.encodeToString(UmFileUtilSe.getMd5Sum(file, buf))

        containerEntryFile.cefMd5 = md5
        containerEntryFile.ceTotalSize = file.length()

        entryFileDao.update(containerEntryFile)


    }

    companion object {

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            OneOffWork()

        }
    }

}

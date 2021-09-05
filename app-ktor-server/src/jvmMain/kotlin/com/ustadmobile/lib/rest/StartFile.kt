package com.ustadmobile.lib.rest

import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import java.nio.file.Files

fun Route.StartFile(){

    get("/startFile"){

        val di : DI by closestDI()
        val repo: UmAppDatabase = di.on(call).direct.instance(DoorTag.TAG_REPO)
        val epubPlugin: EpubTypePluginCommonJvm = di.on(call).direct.instance()

        val epub = File("/home/ustad/server/test.epub")
        val tempDir = Files.createTempDirectory("tmp").toFile()
        val processContext = ProcessContext(DoorUri.parse(tempDir.toURI().toString()),
                params = mutableMapOf())
        val metadataResult = epubPlugin.extractMetadata(DoorUri.parse(epub.toURI().toString()),
                processContext)
        val containerFolder: File = closestDI().on(context).direct.instance(DiTag.TAG_DEFAULT_CONTAINER_DIR)
        if(metadataResult != null){
            val uid = repo.contentEntryDao.insert(metadataResult.entry)
            val job = ContentJobItem(fromUri = epub.toURI().toString(),
                    toUri = containerFolder.toURI().toString(),
                    cjiContentEntryUid = uid)
            epubPlugin.processJob(job, processContext){
            }
            call.respond("OK")
        }
    }

}
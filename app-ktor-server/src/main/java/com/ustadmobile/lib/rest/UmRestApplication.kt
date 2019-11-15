package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile
import javax.naming.InitialContext


private val _restApplicationDb = UmAppDatabase.getInstance(Any(), "UmAppDatabase")


fun Application.umRestApplication(devMode: Boolean = false, db : UmAppDatabase = _restApplicationDb) {

    val adminuser = db.personDao.findByUsername("admin")
    if(adminuser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = db.personDao.insert(adminPerson)
        val adminPass = RandomStringUtils.randomAlphanumeric(8)

        db.personAuthDao.insert(PersonAuth(adminPerson.personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX + encryptPassword(adminPass)))


        val iContext = InitialContext()
        val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as String
        val adminPassFile = File(containerDirPath, "admin.txt")
        if(!adminPassFile.parentFile.isDirectory) {
            adminPassFile.parentFile.mkdirs()
        }

        adminPassFile.writeText(adminPass)
        println("Saved admin password to ${adminPassFile.absolutePath}")
    }

    if(devMode) {
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Options)
            header(HttpHeaders.ContentType)
            anyHost()
        }
    }


    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    install(Routing) {
        ContainerDownload(db)
        H5PImportRoute(db) { url: String, entryUid: Long, urlContent: String, containerUid: Long ->
            downloadH5PUrl(db, url, entryUid, Files.createTempDirectory("h5p").toFile(), urlContent, containerUid)
        }

        LoginRoute(db)
        ContainerMountRoute(db)
        UmAppDatabase_KtorRoute(db, Gson(), File("attachments/UmAppDatabase").absolutePath)
        if(devMode) {

            get("UmAppDatabase/clearAllTables") {
                db.clearAllTables()
                call.respond("OK - cleared")
            }

            get("UmContainer/addContainer"){

                val indexTempDir = 0; val indexTempFile = 1; val indexContainer = 2

                val resourceName = call.request.queryParameters["resource"]
                val entryUid = call.request.queryParameters["entryid"]
                val contentTye = call.request.queryParameters["type"]
                val mimeType = when (contentTye) {
                    "tincan" -> "application/tincan+zip"
                    "epub" -> "application/epub+zip"
                    else -> ""
                }

                handleInvalidRequest(call)

                val preparedRes = prepareResources(resourceName,contentTye!!, entryUid, mimeType)

                val containerManager = ContainerManager(preparedRes[indexContainer] as Container, _restApplicationDb, _restApplicationDb,
                        (preparedRes[indexTempDir] as File).absolutePath)

                val epubZipFile = ZipFile(preparedRes[indexTempFile] as File)
                addEntriesFromZipToContainer((preparedRes[indexTempFile] as File).absolutePath, containerManager)
                epubZipFile.close()
                val containerUid = (preparedRes[indexContainer] as Container).containerUid
                call.respond(containerUid)
            }
        }
    }
}

private suspend fun handleInvalidRequest(call: ApplicationCall){
    val resourceName = call.request.queryParameters["resource"]
    val entryId = call.request.queryParameters["entryid"]
    val resourceType = call.request.queryParameters["type"]
    if((resourceName != null && resourceName.isEmpty()) || resourceName == null
            || (entryId != null && entryId.isEmpty()) || entryId == null ||
            (resourceType != null && resourceType.isEmpty()) || resourceType == null){
        call.respond("Invalid request make sure you have included all resource param")
        return
    }
}


private fun prepareResources(resourceName: String?, path: String, entryId: String?, mimetype: String): Array<Any>{
    val epubContainer = Container()

    if(entryId!= null && entryId.isNotEmpty()){
        epubContainer.containerContentEntryUid = entryId.toLong()
    }

    val tempFile = File.createTempFile("testFile", "tempFile$entryId")

    epubContainer.cntLastModified = tempFile.lastModified()
    epubContainer.mimeType = mimetype
    epubContainer.containerUid = _restApplicationDb.containerDao.insert(epubContainer)

    UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/$path/${resourceName}", tempFile)
    val tempDir = UmFileUtilSe.makeTempDir("testFile", "containerDirTmp")
    return arrayOf(tempDir, tempFile, epubContainer)
}
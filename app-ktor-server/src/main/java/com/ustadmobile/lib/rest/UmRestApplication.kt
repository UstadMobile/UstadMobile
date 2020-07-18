package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
    val iContext = InitialContext()
    val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as? String
            ?: "./build/container"

    if(adminuser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = db.personDao.insert(adminPerson)
        val adminPass = RandomStringUtils.randomAlphanumeric(8)

        db.personAuthDao.insert(PersonAuth(adminPerson.personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX + encryptPassword(adminPass)))




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

    install(CallLogging)

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
        db.preload()

        if(devMode) {

            get("UmAppDatabase/clearAllTables") {
                db.clearAllTables()
                call.respond("OK - cleared")
            }

            get("UmContainer/addContainer"){
                val resourceName = call.request.queryParameters["resource"]
                val entryUid = call.request.queryParameters["entryUid"]
                val contentTye = call.request.queryParameters["type"]
                val mimeType = when (contentTye) {
                    "tincan" -> "application/tincan+zip"
                    "epub" -> "application/epub+zip"
                    else -> null
                }

                if(resourceName == null || entryUid == null || mimeType == null) {
                    call.respond(HttpStatusCode.BadRequest,
                            "Invalid request make sure you have included all resource param")
                    return@get
                }

                val preparedRes = prepareResources(db, resourceName, contentTye!!, entryUid, mimeType)

                val containerManager = ContainerManager(preparedRes.tmpContainer, db, db,
                        preparedRes.tempDir.absolutePath)

                addEntriesFromZipToContainer(preparedRes.tempFile.absolutePath, containerManager)
                val containerUid = preparedRes.tmpContainer.containerUid
                call.respond(containerUid)
            }
        }
    }
}

data class PreparedResource(val tempDir: File, val tempFile: File, val tmpContainer: Container)

private fun prepareResources(db: UmAppDatabase, resourceName: String?, path: String, entryId: String?, mimetype: String): PreparedResource{
    val epubContainer = Container()

    if(entryId!= null && entryId.isNotEmpty()){
        epubContainer.containerContentEntryUid = entryId.toLong()
    }

    val tempFile = File.createTempFile("testFile", "tempFile$entryId")

    epubContainer.cntLastModified = tempFile.lastModified()
    epubContainer.mimeType = mimetype
    epubContainer.containerUid = db.containerDao.insert(epubContainer)

    UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/$path/${resourceName}", tempFile)
    val tempDir = UmFileUtilSe.makeTempDir("testFile", "containerDirTmp")
    return PreparedResource(tempDir, tempFile, epubContainer)
}
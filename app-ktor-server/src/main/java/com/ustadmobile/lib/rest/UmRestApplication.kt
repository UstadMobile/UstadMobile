package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseCallback
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.util.encryptPassword
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
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
        UmAppDatabase_KtorRoute(db, Gson(), File("attachments/UmAppDatabase").absolutePath)
        if(devMode) {
            get("UmAppDatabase/clearAllTables") {
                db.clearAllTables()
                call.respond("OK - cleared")
            }
        }
    }
}
package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.doublePbkdf2Hash
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.requireDbAndRepo
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.randomString
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

internal fun UmAppDatabase.insertDefaultSite(trackerUrl: String) {
    val (db, repo) = requireDbAndRepo()
    val site = db.siteDao.getSite()
    if(site == null) {
        repo.siteDao.insert(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
            authSalt = randomString(20)
            torrentAnnounceUrl = trackerUrl
        })
    }else if(site.torrentAnnounceUrl != trackerUrl){
        throw Exception("tracker url not same, this cannot be changed")
    }
}

/**
 * Initialize the admin account. This must be done on the repo
 */
suspend fun UmAppDatabase.initAdminUser(
    endpoint: Endpoint,
    di: DI
) {
    if(this !is DoorDatabaseRepository)
        throw IllegalStateException("initAdminUser must be called on repo!")

    val passwordFilePath = di.on(endpoint).direct
        .instance<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT).absolutePath
    val adminUser = personDao.findByUsername("admin")

    if (adminUser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = insertPersonAndGroup(adminPerson).personUid

        //Remove lower case l, upper case I, and the number 1
        val adminPass = randomString(10, "abcdefghijkmnpqrstuvxwyzABCDEFGHJKLMNPQRSTUVWXYZ23456789")

        val authManager: AuthManager = di.on(endpoint).direct.instance()
        authManager.setAuth(adminPerson.personUid, adminPass)

        val adminPassFile = File(passwordFilePath, "admin.txt")
        if (!adminPassFile.parentFile.isDirectory) {
            adminPassFile.parentFile.mkdirs()
        }

        val hexFile = File(passwordFilePath, "hex.txt")

        val repo: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
        val salt = repo.siteDao.getSiteAsync()!!.authSalt!!

        hexFile.writeText(
            adminPass.doublePbkdf2Hash(salt, di.direct.instance()).toHexString())

        grantScopedPermission(adminPerson, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES)

        adminPassFile.writeText(adminPass)
        println("Saved admin password to ${adminPassFile.absolutePath}")
    }
}

fun UmAppDatabase.ktorInitRepo(trackerUrl: String) {
    insertDefaultSite(trackerUrl)

    runBlocking {
        roleDao.insertDefaultRolesIfRequired()
    }
}
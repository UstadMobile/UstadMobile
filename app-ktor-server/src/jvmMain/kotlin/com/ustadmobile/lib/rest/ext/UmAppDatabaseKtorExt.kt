package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.ext.requireDbAndRepo
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.randomString
import kotlinx.coroutines.runBlocking
import java.io.File

internal fun UmAppDatabase.insertDefaultSite() {
    val (db, repo) = requireDbAndRepo()
    if(db.siteDao.getSite() == null) {
        repo.siteDao.insert(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
            authSalt = randomString(20)
        })
    }
}

fun UmAppDatabase.ktorInitDbWithRepo(repo: UmAppDatabase, passwordFilePath: String) {
    repo.insertDefaultSite()

    val adminuser = personDao.findByUsername("admin")

    if (adminuser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = runBlocking { repo.insertPersonAndGroup(adminPerson).personUid }

        //Remove lower case l, upper case I, and the number 1
        val adminPass = randomString(10, "abcdefghijkmnpqrstuvxwyzABCDEFGHJKLMNPQRSTUVWXYZ23456789")

        personAuthDao.insert(PersonAuth(adminPerson.personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX + encryptPassword(adminPass)))


        val adminPassFile = File(passwordFilePath, "admin.txt")
        if (!adminPassFile.parentFile.isDirectory) {
            adminPassFile.parentFile.mkdirs()
        }

        runBlocking {
            repo.grantScopedPermission(adminPerson, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES)
        }


        adminPassFile.writeText(adminPass)
        println("Saved admin password to ${adminPassFile.absolutePath}")
    }

    runBlocking {
        repo.roleDao.insertDefaultRolesIfRequired()
    }
}
package com.ustadmobile.lib.rest.ext

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.ArrayList

fun UmAppDatabase.ktorInitDbWithRepo(repo: UmAppDatabase, passwordFilePath: String) {
    if(siteDao.getSite() == null) {
        repo.siteDao.insert(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
        })
    }

    val adminuser = personDao.findByUsername("admin")

    if (adminuser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = runBlocking { repo.insertPersonAndGroup(adminPerson).personUid }

        //Remove lower case l, upper case I, and the number 1
        val adminPass = RandomStringUtils.random(10, "abcdefghijkmnpqrstuvxwyzABCDEFGHJKLMNPQRSTUVWXYZ23456789")

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
package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import java.io.File

fun UmAppDatabase.ktorInitDbWithRepo(repo: UmAppDatabase, passwordFilePath: String) {
    if(workSpaceDao.getWorkSpace() == null) {
        repo.workSpaceDao.insert(WorkSpace().apply {
            uid = 1L
            name = "UstadmobileWorkspace"
            guestLogin = true
            registrationAllowed = true
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

        adminPassFile.writeText(adminPass)
        println("Saved admin password to ${adminPassFile.absolutePath}")
    }

    runBlocking {
        repo.roleDao.insertDefaultRolesIfRequired()
    }
}
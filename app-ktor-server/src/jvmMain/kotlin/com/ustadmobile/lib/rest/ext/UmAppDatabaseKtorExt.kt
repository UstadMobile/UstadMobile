package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.util.encryptPassword
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import java.io.File

fun UmAppDatabase.ktorInit(passwordFilePath: String) {
    val adminuser = personDao.findByUsername("admin")


    if(workSpaceDao.getWorkSpace() == null) {
        workSpaceDao.insert(WorkSpace().apply {
            uid = 1L
            name = "UstadmobileWorkspace"
            guestLogin = true
            registrationAllowed = true
        })
    }


    if (adminuser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = personDao.insert(adminPerson)

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

    runBlocking { roleDao.insertDefaultRolesIfRequired() }
}
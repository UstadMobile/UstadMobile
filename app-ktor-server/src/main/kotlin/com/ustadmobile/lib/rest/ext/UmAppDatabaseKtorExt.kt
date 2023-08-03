package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.doublePbkdf2Hash
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.requireDbAndRepo
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import io.github.aakira.napier.Napier
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File


fun UmAppDatabase.insertCourseTerminology(di: DI){
    val (db, repo) = requireDbAndRepo()
    val termList = db.courseTerminologyDao.findAllCourseTerminologyList()
    val supportLangConfig: SupportedLanguagesConfig = di.direct.instance()
    if(termList.isEmpty()) {

        val impl: UstadMobileSystemImpl by di.instance()
        val json: Json by di.instance()

        val languageOptions = supportLangConfig.supportedUiLanguages
        val terminologyList = mutableListOf<CourseTerminology>()

        languageOptions.forEach { pair ->

            terminologyList.add(CourseTerminology().apply {
                ctUid = (pair.langCode[0].code shl(8)) + (pair.langDisplay[1].code).toLong()
                ctTitle = impl.getString(pair.langCode,
                    MessageID.standard, Any()) + " - " + pair.langDisplay

                ctTerminology = json.encodeToString(
                    MapSerializer(String.serializer(), String.serializer()),
                    com.ustadmobile.core.controller.TerminologyKeys.TERMINOLOGY_ENTRY_MESSAGE_ID
                        .map { it.key to impl.getString(pair.langCode, it.value, Any()) }
                        .toMap()
                )
            })
        }

        repo.courseTerminologyDao.insertList(terminologyList)
    }
}

/**
 * Initialize the admin account. This must be done on the repo
 */
suspend fun UmAppDatabase.initAdminUser(
    endpoint: Endpoint,
    authManager: AuthManager,
    di: DI,
    defaultPassword: String? = null,
) {
    val passwordFilePath = di.on(endpoint).direct
        .instance<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT).absolutePath
    val adminUser = personDao.findByUsername("admin")

    if (adminUser == null) {
        val adminPerson = Person("admin", "Admin", "User")
        adminPerson.admin = true
        adminPerson.personUid = insertPersonAndGroup(adminPerson).personUid

        //Remove lower case l, upper case I, and the number 1
        val adminPass = defaultPassword
            ?: randomString(10, "abcdefghijkmnpqrstuvxwyzABCDEFGHJKLMNPQRSTUVWXYZ23456789")

        authManager.setAuth(adminPerson.personUid, adminPass)

        val adminPassFile = File(passwordFilePath, "admin.txt")
        if (!adminPassFile.parentFile.isDirectory) {
            adminPassFile.parentFile.mkdirs()
        }

        val saltFile = File(passwordFilePath, "salt-${systemTimeInMillis()}.txt")

        val repo: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
        val salt = repo.siteDao.getSiteAsync()!!.authSalt!!

        saltFile.writeText(
            "$salt / $adminPass"
        )

        grantScopedPermission(adminPerson, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES)

        adminPassFile.writeText(adminPass)
        Napier.i("Saved admin password to ${adminPassFile.absolutePath}")
    }
}

fun UmAppDatabase.ktorInitRepo(di: DI) {
    insertCourseTerminology(di)
}
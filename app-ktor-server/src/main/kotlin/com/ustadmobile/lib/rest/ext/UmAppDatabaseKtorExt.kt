package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.DiTag
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
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.person.AddNewPersonUseCase

fun UmAppDatabase.insertCourseTerminology(di: DI){
    val termList = courseTerminologyDao().findAllCourseTerminologyList()
    val supportLangConfig: SupportedLanguagesConfig = di.direct.instance()

    if(termList.isEmpty()) {

        val impl: UstadMobileSystemImpl by di.instance()
        val json: Json by di.instance()

        val languageOptions = supportLangConfig.supportedUiLanguages
        val terminologyList = mutableListOf<CourseTerminology>()

        languageOptions.forEach { pair ->

            terminologyList.add(CourseTerminology().apply {
                ctUid = (pair.langCode[0].code shl(8)) + (pair.langDisplay[1].code).toLong()
                ctTitle = impl.getString(MR.strings.standard, pair.langCode) + " - " + pair.langDisplay

                ctTerminology = json.encodeToString(
                    MapSerializer(String.serializer(), String.serializer()),
                    com.ustadmobile.core.controller.TerminologyKeys.TERMINOLOGY_ENTRY_MESSAGE_ID
                        .map { it.key to impl.getString(it.value, pair.langCode) }
                        .toMap()
                )
            })
        }

        courseTerminologyDao().insertList(terminologyList)
    }
}

/**
 * Initialize the admin account.
 */
suspend fun UmAppDatabase.initAdminUser(
    endpoint: Endpoint,
    authManager: AuthManager,
    di: DI,
    defaultPassword: String? = null,
) {
    val passwordFilePath = di.on(endpoint).direct
        .instance<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT).absolutePath

    val adminUser = personDao().findByUsername("admin")

    if (adminUser == null) {
        val addNewPersonUseCase: AddNewPersonUseCase = di.on(endpoint).direct.instance()

        val adminPerson = Person(username = "admin", firstNames = "Admin", lastName = "User")

        adminPerson.personUid = addNewPersonUseCase(
            adminPerson, systemPermissions = Long.MAX_VALUE,
        )

        //Remove lower case l, upper case I, and the number 1
        val adminPass = defaultPassword
            ?: randomString(10, "abcdefghijkmnpqrstuvxwyzABCDEFGHJKLMNPQRSTUVWXYZ23456789")

        authManager.setAuth(adminPerson.personUid, adminPass)

        val adminPassFile = File(passwordFilePath, "admin.txt")
        if (!adminPassFile.parentFile.isDirectory) {
            adminPassFile.parentFile.mkdirs()
        }

        val saltFile = File(passwordFilePath, "salt-${systemTimeInMillis()}.txt")

        val salt = siteDao().getSiteAsync()!!.authSalt!!

        saltFile.writeText("$salt / $adminPass")


        adminPassFile.writeText(adminPass)
        Napier.i("Saved admin password to ${adminPassFile.absolutePath}")
    }
}

fun UmAppDatabase.ktorInitDb(di: DI) {
    insertCourseTerminology(di)
}

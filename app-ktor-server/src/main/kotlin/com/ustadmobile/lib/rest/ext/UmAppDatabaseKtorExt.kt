package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.MR

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

fun UmAppDatabase.ktorInitDb(di: DI) {
    insertCourseTerminology(di)
}

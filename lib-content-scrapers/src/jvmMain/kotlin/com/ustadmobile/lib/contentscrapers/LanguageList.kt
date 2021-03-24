package com.ustadmobile.lib.contentscrapers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.lib.contentscrapers.ScraperConstants.LANGUAGE_LIST_LOCATION
import com.ustadmobile.lib.db.entities.Language
import java.io.IOException
import java.util.*


/**
 * A list of all the ISO-639-3 language code can be found at https://iso639-3.sil.org/code_tables/639/data
 * and downloaded at https://iso639-3.sil.org/code_tables/download_tables
 *
 * The data is in .tab format that can be converted to JSON format( i converted to CSV first to modify fields)
 *
 */
class LanguageList {

    @Throws(IOException::class)
    fun addAllLanguages() {

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val langList = gson.fromJson<ArrayList<Language>>(javaClass
                .getResourceAsStream(LANGUAGE_LIST_LOCATION).readString(),
                object : TypeToken<List<Language>>() {

        }.type)

        val db = UmAppDatabase.getInstance(Any())
        val repository = db //db.getRepository("https://localhost", "")
        val langDao = repository.languageDao
        if (langDao.totalLanguageCount() < 7000) {
            langDao.insertList(langList)
        }
    }

}

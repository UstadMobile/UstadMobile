package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Language

class LanguageDaoJs: LanguageDao() {
    override suspend fun insertListAsync(languageList: List<Language>) {
        TODO("Not yet implemented")
    }

    override fun findLanguagesAsSource(
        sortOrder: Int,
        searchText: String
    ): DoorDataSourceFactory<Int, Language> {
        TODO("Not yet implemented")
    }

    override fun findLanguagesList(): List<Language> {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Language? {
        TODO("Not yet implemented")
    }

    override fun findByTwoCode(langCode: String): Language? {
        TODO("Not yet implemented")
    }

    override suspend fun findByTwoCodeAsync(langCode: String): Language? {
        TODO("Not yet implemented")
    }

    override fun findByThreeCode(langCode: String): Language? {
        TODO("Not yet implemented")
    }

    override fun totalLanguageCount(): Int {
        TODO("Not yet implemented")
    }

    override fun update(entity: Language) {
        TODO("Not yet implemented")
    }

    override fun findByUid(primaryLanguageUid: Long): Language? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(primaryLanguageUid: Long): Language? {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: Language): Int {
        TODO("Not yet implemented")
    }

    override fun findAllLanguageLive(): DoorLiveData<List<Language>> {
        TODO("Not yet implemented")
    }

    override fun findByUidList(uidList: List<Long>): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun toggleVisibilityLanguage(
        toggleVisibility: Boolean,
        selectedItem: List<Long>
    ) {
        TODO("Not yet implemented")
    }

    override fun replaceList(entityList: List<Language>) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: Language): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: Language): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Language>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Language>) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            Language().apply {
                langUid = 1
                name = "English"
                iso_639_2_standard = "en"
            },
            Language().apply {
                langUid = 2
                name = "Swahili"
                iso_639_2_standard = "sw"
            },
            Language().apply {
                langUid = 3
                name = "Arabic"
                iso_639_2_standard = "ar"
            },
            Language().apply {
                langUid = 4
                name = "Farsi"
                iso_639_2_standard = "fa"
            },
            Language().apply {
                langUid = 5
                name = "Pashdo"
                iso_639_2_standard = "pa"
            },
            Language().apply {
                langUid = 6
                name = "Tajik"
                iso_639_2_standard = "tg"
            },
            Language().apply {
                langUid = 7
                name = "Urdu"
                iso_639_2_standard = "ur"
            },

        )
    }
}
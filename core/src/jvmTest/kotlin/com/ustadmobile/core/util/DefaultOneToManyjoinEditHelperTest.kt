package com.ustadmobile.core.util

import com.google.gson.Gson
import org.mockito.kotlin.mock
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenterJsonLoader
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class DefaultOneToManyjoinEditHelperTest {

    lateinit var di: DI

    lateinit var mockEditPresenter: UstadEditPresenterJsonLoader

    @Before
    fun setup() {
        di = DI {
            bind<Gson>() with singleton { Gson() }
        }

        mockEditPresenter = mock {

        }
    }

    @Test
    fun givenEntitiesToInsert_whenSavedThenRestored_shouldMatch() {
        val testEntity = ContentEntry().also {
            it.title = "Test Entry"
        }

        val joinEditHelper1 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
            "statename", ListSerializer(ContentEntry.serializer()),
            ListSerializer(ContentEntry.serializer()),
            mockEditPresenter, di, ContentEntry::class) { contentEntryUid = it}

        joinEditHelper1.onEditResult(testEntity)

        val saveMap = mutableMapOf<String, String>()

        joinEditHelper1.onSaveState(saveMap)

        val joinEditHelper2 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ListSerializer(ContentEntry.serializer()),
                ListSerializer(ContentEntry.serializer()),
                mockEditPresenter, di, ContentEntry::class) { contentEntryUid = it}

        joinEditHelper2.onLoadFromJsonSavedState(saveMap)

        Assert.assertEquals("After edit result, save and load, list has been restored on edit helper",
            joinEditHelper1.entitiesToInsert, joinEditHelper2.entitiesToInsert)

    }

    @Test
    fun givenEmptyList_whenSavedThenRestored_shouldMatch() {
        val joinEditHelper1 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ListSerializer(ContentEntry.serializer()),
                ListSerializer(ContentEntry.serializer()),
                mockEditPresenter, di, ContentEntry::class) { contentEntryUid = it}


        val saveMap = mutableMapOf<String, String>()

        joinEditHelper1.onSaveState(saveMap)

        val joinEditHelper2 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ListSerializer(ContentEntry.serializer()),
                ListSerializer(ContentEntry.serializer()),
                mockEditPresenter, di, ContentEntry::class) { contentEntryUid = it}

        joinEditHelper2.onLoadFromJsonSavedState(saveMap)

        Assert.assertEquals("After edit result, save and load, list has been restored on edit helper",
                joinEditHelper1.entitiesToInsert, joinEditHelper2.entitiesToInsert)
    }

}
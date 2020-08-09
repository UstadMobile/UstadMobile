package com.ustadmobile.core.util

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.builtins.list
import org.junit.Assert
import org.junit.Test

class DefaultOneToManyjoinEditHelperTest {

    @Test
    fun givenEntitiesToInsert_whenSavedThenRestored_shouldMatch() {
        val testEntity = ContentEntry().also {
            it.title = "Test Entry"
        }

        val joinEditHelper1 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
            "statename", ContentEntry.serializer().list, ContentEntry.serializer().list,
            null) { contentEntryUid = it}

        joinEditHelper1.onEditResult(testEntity)

        val saveMap = mutableMapOf<String, String>()

        joinEditHelper1.onSaveState(saveMap)

        val joinEditHelper2 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ContentEntry.serializer().list, ContentEntry.serializer().list,
                null) { contentEntryUid = it}

        joinEditHelper2.onLoadFromJsonSavedState(saveMap)

        Assert.assertEquals("After edit result, save and load, list has been restored on edit helper",
            joinEditHelper1.entitiesToInsert, joinEditHelper2.entitiesToInsert)

    }

    @Test
    fun givenEmptyList_whenSavedThenRestored_shouldMatch() {
        val joinEditHelper1 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ContentEntry.serializer().list, ContentEntry.serializer().list,
                null) { contentEntryUid = it}


        val saveMap = mutableMapOf<String, String>()

        joinEditHelper1.onSaveState(saveMap)

        val joinEditHelper2 = DefaultOneToManyJoinEditHelper<ContentEntry>(ContentEntry::contentEntryUid,
                "statename", ContentEntry.serializer().list, ContentEntry.serializer().list,
                null) { contentEntryUid = it}

        joinEditHelper2.onLoadFromJsonSavedState(saveMap)

        Assert.assertEquals("After edit result, save and load, list has been restored on edit helper",
                joinEditHelper1.entitiesToInsert, joinEditHelper2.entitiesToInsert)
    }

}
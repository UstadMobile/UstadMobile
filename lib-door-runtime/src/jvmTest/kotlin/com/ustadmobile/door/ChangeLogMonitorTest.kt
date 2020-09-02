package com.ustadmobile.door

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

class ChangeLogMonitorTest {

    lateinit var mockDb: DoorDatabase

    lateinit var mockRepo: DoorDatabaseRepository


    @Before
    fun setup() {
        mockDb = mock{ }
        mockRepo = mock {
            on { tableIdMap }.thenReturn(mapOf("Test" to 42))
        }
    }

    @Test
    fun givenEmptyDb_whenChangeHappens_thenShouldDispatchToRepo(){
        val changeLogMonitor = ChangeLogMonitor(mockDb, mockRepo)
        changeLogMonitor.onTablesChanged(listOf("Test"))

        verifyBlocking(mockRepo, timeout(2000)) { onPendingChangeLog(argWhere<Set<Int>> { 42 in it })  }
    }

    @Test
    fun givenEmptyDb_whenMultipleChangesHappen_thenShouldBeBatched() {
        val changeLogMonitor = ChangeLogMonitor(mockDb, mockRepo)

        changeLogMonitor.onTablesChanged(listOf("Test"))
        changeLogMonitor.onTablesChanged(listOf("Test"))
        changeLogMonitor.onTablesChanged(listOf("Test"))

        Thread.sleep(400)
        verifyBlocking(mockRepo, timeout(2000).times(1)) {
            onPendingChangeLog(argWhere{ 42 in it && it.size == 1})
        }
    }



}
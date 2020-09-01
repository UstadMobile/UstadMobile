package com.ustadmobile.door

import com.nhaarman.mockitokotlin2.argWhere
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

class ChangeLogMonitorSqliteTest {

    lateinit var memDb: Connection

    lateinit var mockDb: DoorDatabase

    lateinit var mockRepo: DoorDatabaseRepository


    @Before
    fun setup() {
        memDb = DriverManager.getConnection("jdbc:sqlite::memory:")
        mockDb = mock{
            on { openConnection() }.thenReturn(memDb)
        }

        mockRepo = mock {
            on { tableIdMap }.thenReturn(mapOf("Test" to 42))
        }
    }

    @Test
    fun givenEmptyDb_whenChangeHappens_thenShouldDispatchToRepo(){
        val createStmt = memDb.createStatement()
        createStmt.execute("CREATE TABLE Test(id INT PRIMARY KEY NOT NULL, aNumber INT NOT NULL DEFAULT 0)")

        val changeLogMonitor = ChangeLogMonitorSqlite(mockDb, mockRepo)

        createStmt.executeUpdate("INSERT INTO Test VALUES(1, 2)")
        verifyBlocking(mockRepo, timeout(2000)) { onPendingChangeLog(argWhere<Set<Int>> { 42 in it })  }
        createStmt.close()
    }

    @Test
    fun givenEmptyDb_whenMultipleChangesHappen_thenShouldBeBatched() {
        val createStmt = memDb.createStatement()
        createStmt.execute("CREATE TABLE Test(id INT PRIMARY KEY NOT NULL, aNumber INT NOT NULL DEFAULT 0)")

        val changeLogMonitor = ChangeLogMonitorSqlite(mockDb, mockRepo)

        createStmt.executeUpdate("INSERT INTO Test VALUES(1, 2)")
        createStmt.executeUpdate("INSERT INTO Test VALUES(2, 3)")
        createStmt.executeUpdate("INSERT INTO Test VALUES(3, 4)")

        Thread.sleep(400)
        verifyBlocking(mockRepo, timeout(2000).times(1)) {
            onPendingChangeLog(argWhere{ 42 in it && it.size == 1})
        }
        createStmt.close()
    }



}
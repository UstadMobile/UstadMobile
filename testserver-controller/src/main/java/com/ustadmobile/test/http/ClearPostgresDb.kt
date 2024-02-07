package com.ustadmobile.test.http

import com.ustadmobile.door.jdbc.ext.mapRows
import com.ustadmobile.door.triggers.dropDoorTriggersAndReceiveViews
import kotlinx.coroutines.runBlocking
import java.sql.DriverManager

/**
 * Clears a postgres database, ready for a new test.
 */
fun clearPostgresDb(
    dbUrl: String,
    dbUsername: String,
    dbPassword: String
) {
    try {
        DriverManager.getConnection(dbUrl, dbUsername, dbPassword).use { connection ->
            runBlocking {
                connection.dropDoorTriggersAndReceiveViews(
                    triggerFilter = null,
                    functionFilter = null,
                )
            }

            connection.createStatement().use { stmt ->
                val tableNames = stmt.executeQuery("""
                    SELECT table_name 
                      FROM information_schema.tables 
                     WHERE table_type='BASE TABLE'
                       AND table_schema='public'
                """).use {
                    it.mapRows { it.getString(1) }
                }

                tableNames.forEach {
                    stmt.addBatch("DROP TABLE $it")
                }
                stmt.executeBatch()
            }
        }

        println("TestServer controller: cleared $dbUrl")
    }catch(e: Throwable) {
        println("Exception clearing postgres")
        e.printStackTrace()
        throw e
    }


}
package com.ustadmobile.core.db.ext

import com.ustadmobile.door.DoorConstants
import com.ustadmobile.door.jdbc.ext.mapRows
import com.ustadmobile.door.migration.DoorMigration
import com.ustadmobile.door.migration.DoorMigrationSync

fun MigrateMvvm(
    onRan: () -> Unit,
) : DoorMigration = DoorMigrationSync(109, 120) { db ->
    //Only postgres is supported here
    db.connection.createStatement().use { stmt ->
        //1: Drop all triggers
        val triggerAndTableNames = stmt.executeQuery(
            """
        SELECT trigger_name, event_object_table
          FROM information_schema.triggers
        """
        ).use { results ->
            results.mapRows {resultSet ->
                resultSet.getString(1) to resultSet.getString(2)
            }
        }

        triggerAndTableNames.distinct().forEach {
            stmt.addBatch("DROP TRIGGER ${it.first} ON ${it.second}")
        }
        stmt.executeBatch()

        //2: Drop all functions
        val functionNames = stmt.executeQuery("""
        SELECT routine_name
          FROM information_schema.routines
         WHERE routine_type = 'FUNCTION'
           AND routine_schema = 'public'
    """).use { results ->
            results.mapRows { resultSet ->
                resultSet.getString(1)
            }
        }

        functionNames.distinct().forEach {
            stmt.addBatch("DROP FUNCTION $it")
        }
        stmt.executeBatch()

        //3: drop all old receiveviews
        val doorReceiveViewNames = stmt.executeQuery("""
        SELECT table_name 
          FROM information_schema.views
         WHERE lower(table_name) LIKE '%${DoorConstants.RECEIVE_VIEW_SUFFIX.lowercase()}'
    """).use { results ->
            results.mapRows { resultSet ->
                resultSet.getString(1)
            }
        }

        doorReceiveViewNames.forEach {
            stmt.addBatch("DROP VIEW $it")
        }
        stmt.executeBatch()

        //4: Drop all ReplicateEntity tables
        val replicateTableNames = stmt.executeQuery("""
        SELECT table_name 
          FROM information_schema.tables 
         WHERE table_type='BASE TABLE'
           AND table_schema='public'
           AND table_name LIKE '%replicate'
    """).use { results ->
            results.mapRows { it.getString(1) }
        }
        replicateTableNames.forEach {
            stmt.addBatch("DROP TABLE $it")
        }
        stmt.executeBatch()

        //5: Drop old tables
        val oldTables = listOf(
            "clazzassignmentcontentjoin", "clazzcontentjoin", "discussiontopic",
            "grouplearningsession", "learnergroup", "learnergroupmember",
            "grouplearningsession", "changelog", "zombieattachmentdata",
            "entityrole", "auditlog", "customfield", "customfieldvalue",
            "CustomFieldValueOption", "daterange", "clazzassignmentrollup",
            "daterange", "ClazzAssignmentRollUp")
        oldTables.forEach {
            stmt.executeUpdate("DROP TABLE IF EXISTS $it")
        }

        //6 Update ContentEntryParentChildJoin - Library Root content entry uid was changed
        stmt.executeUpdate("""
        UPDATE ContentEntryParentChildJoin
           SET cepcjparentcontententryuid = 1
         WHERE cepcjparentcontententryuid = -4103245208651563007
    """)
    }

    onRan()
}
package com.ustadmobile.core.db.ext

import com.ustadmobile.core.domain.filterusername.FilterUsernameUseCase
import com.ustadmobile.door.migration.DoorMigrationStatementList


val MIGRATE_USERNAME_SERVER = DoorMigrationStatementList(202, 203) { db ->
    val filterUsernameUseCase = FilterUsernameUseCase()
    val statements = mutableListOf<String>()

    db.connection.createStatement().use { stmt ->
        // Process usernames in batches
        var offset = 0
        val batchSize = 1000

        while(true) {
            val users = stmt.executeQuery("""
                SELECT personUid, username 
                FROM Person 
                ORDER BY personUid 
                LIMIT $batchSize 
                OFFSET $offset
            """).use { results ->
                buildList {
                    while(results.next()) {
                        add(results.getLong(1) to (results.getString(2) ?: ""))
                    }
                }
            }

            if(users.isEmpty()) break

            users.forEach { (personUid, currentUsername) ->
                val filteredUsername = filterUsernameUseCase(currentUsername, "_")
                if (currentUsername != filteredUsername) {
                    statements.add("""
                        UPDATE Person 
                        SET username = '$filteredUsername'
                        WHERE personUid = $personUid
                    """.trimIndent())
                }
            }

            offset += batchSize
        }
    }

    statements
}

val MIGRATE_USERNAME_CLIENT = DoorMigrationStatementList(202, 203) { _ ->
    emptyList()
}
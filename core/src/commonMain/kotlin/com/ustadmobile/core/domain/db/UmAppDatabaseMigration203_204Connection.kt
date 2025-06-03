package com.ustadmobile.core.domain.db

import com.ustadmobile.core.util.ext.toByteArray
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.jdbc.Connection
import com.ustadmobile.door.jdbc.ext.executeQueryAsyncKmp
import com.ustadmobile.door.jdbc.ext.executeUpdateAsync
import com.ustadmobile.door.jdbc.ext.useResults
import com.ustadmobile.door.jdbc.ext.useStatementAsync
import com.ustadmobile.xxhashkmp.XXHasher64Factory

suspend fun Connection.migrate203_204AddStatementEntityContextRegHash(
    xxHasher64Factory: XXHasher64Factory,
    dbType: Int,
) {
    createStatement().useStatementAsync {
        val colTypeName = if(dbType == DoorDbType.SQLITE) "INTEGER" else "BIGINT"
        it.executeUpdateAsync(
            "ALTER TABLE StatementEntity ADD COLUMN contextRegistrationHash $colTypeName NOT NULL DEFAULT 0"
        )
    }

    prepareStatement("""
         SELECT DISTINCT StatementEntity.contextRegistrationHi, StatementEntity.contextRegistrationLo,
           FROM StatementEntity
          WHERE StatementEntity.contextRegistrationHash = 0
            AND NOT(StatementEntity.statementIdHi = 0 AND StatementEntity.statementIdLo = 0)
          LIMIT 1000  
    """).useStatementAsync { selectStmtsToUpdate ->
        prepareStatement("""
            UPDATE StatementEntity
               SET contextRegistrationHash = ?
             WHERE contextRegistrationHi = ?
               AND contextRegistrationLo = ?   
        """).useStatementAsync  { updateStatements ->
            var updateCount: Int
            do {
                updateCount = 0
                selectStmtsToUpdate.executeQueryAsyncKmp().useResults { results ->
                    while(results.next()) {
                        val regHi = results.getLong("contextRegistrationHi")
                        val regLo = results.getLong("contextRegistrationLo")
                        val hasher = xxHasher64Factory.newHasher(0)
                        hasher.update(regHi.toByteArray())
                        hasher.update(regLo.toByteArray())
                        val hash = hasher.digest()
                        updateStatements.setLong(1, hash)
                        updateStatements.setLong(2, regHi)
                        updateStatements.setLong(3, regLo)
                        updateStatements.executeUpdate()

                        updateCount++
                    }
                }
            }while(updateCount > 0)
        }
    }
}

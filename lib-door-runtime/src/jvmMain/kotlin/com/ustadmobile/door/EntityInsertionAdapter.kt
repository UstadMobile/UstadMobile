package com.ustadmobile.door

import java.sql.Connection
import java.sql.PreparedStatement

abstract class EntityInsertionAdapter<T>(dbType: Int) {

    abstract fun bindPreparedStmtToEntity(stmt: PreparedStatement, entity: T)

    abstract fun makeSql(): String

    fun insertList(entities: List<T>, con: Connection, dbType: Int) {
        var stmt = null as PreparedStatement?
        try {
            con.autoCommit = false
            stmt = con.prepareStatement(makeSql())
            for(entity in entities) {
                bindPreparedStmtToEntity(stmt, entity)
                stmt.executeUpdate()
            }
        }finally {
            con.autoCommit = true
            con.close()
        }
    }


}


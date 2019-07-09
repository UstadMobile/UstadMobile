package com.ustadmobile.door

import java.sql.PreparedStatement

actual class SimpleDoorQuery : DoorQuery {

    private lateinit var sql: String

    private var queryParams: Array<Any>? = null

    actual constructor(sql: String, values: Array<Any>?) {
        this.sql = sql
        this.queryParams = values
    }

    actual constructor(sql: String) {
        this.sql = sql
    }

    override fun getSql() = sql

    override fun getArgCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindToPreparedStmt(stmt: PreparedStatement) {
        val paramsToBind = queryParams
        if(paramsToBind != null) {
            var paramIndex = 1
            for(param in paramsToBind) {
                stmt.setObject(paramIndex++, param)
            }
        }
    }

}
package com.ustadmobile.door

import java.sql.PreparedStatement

actual interface DoorQuery {

    actual fun getSql(): String

    actual fun getArgCount(): Int

    fun bindToPreparedStmt(stmt: PreparedStatement)
}
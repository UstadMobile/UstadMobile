package com.ustadmobile.door

actual class SimpleDoorQuery actual constructor(sql: String, values: Array<out Any?>?) : DoorQuery {

    override fun getSql() = throw RuntimeException("Raw SQL Query not allowed on Javascript")

    override fun getArgCount() = throw RuntimeException("Raw SQL Query not allowed on Javascript")


}
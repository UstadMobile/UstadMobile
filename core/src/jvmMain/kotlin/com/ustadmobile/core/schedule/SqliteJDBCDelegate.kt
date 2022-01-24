package com.ustadmobile.core.schedule

import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.sql.ResultSet

/**
 * JDBC Delegate for Quartz Scheduler. SLQite does not support getBlob and streaming, so we need to
 * get the actual bytes and thne use ByteArrayInputStream
 */
class SqliteJDBCDelegate : StdJDBCDelegate(){

    override fun getObjectFromBlob(rs: ResultSet, colName: String): Any? {
        val bytes = rs.getBytes(colName)

        return if(bytes.isNotEmpty()) {
            ObjectInputStream(ByteArrayInputStream(bytes)).use {
                it.readObject()
            }
        }else {
            null
        }
    }

    override fun getJobDataFromBlob(rs: ResultSet, colName: String): Any? {
        return getObjectFromBlob(rs, colName)
    }
}

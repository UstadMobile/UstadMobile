package com.ustadmobile.core.schedule

import javax.naming.InitialContext
import javax.sql.DataSource

/**
 * Initialize the Quartz Database using the resource SQL to create tables if required. This
 */
fun InitialContext.initQuartzDb(jndiName: String) {
    //From JDK9+ if we cannot use a class from another module to read resources in this module
    class InternalClass

    val quartzDs = lookup(jndiName) as DataSource
    val sqlStr = InternalClass::class.java.getResourceAsStream(
        "/org/quartz/impl/jdbcjobstore/tables_hsqldb.sql"
    )?.readBytes()?.decodeToString()
        ?: throw IllegalStateException("Could not open /org/quartz/impl/jdbcjobstore/tables_hsqldb.sql")

    quartzDs.connection.use { connection ->
        connection.metaData.getTables(null, null, "%", arrayOf("TABLE")).use { result ->
            if(!result.next()) {
                //This is an empty database
                connection.createStatement().use { stmt ->
                    sqlStr.lines().filter {
                        !it.startsWith("--") //Filter out comments.
                    }
                    .joinToString(separator = " ")
                    .split(";")
                    .filter { it.isNotBlank() }
                    .forEach {
                        try {
                            stmt.executeUpdate(it)
                        }catch(e: Throwable) {
                            println("Exception running: $it")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

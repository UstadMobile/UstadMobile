package com.ustadmobile.lib.rest

import javax.naming.InitialContext
import javax.sql.DataSource

/**
 * Initialize the Quartz Database using the resource SQL to create tables if required. The resource
 * is simply the schema as per the HypersonicSQL statements that are provided in the Quartz distribution
 */
fun InitialContext.initQuartzDb(jndiName: String) {
    //From JDK9+ if we cannot use a class from another module to read resources in this module
    class InternalClass

    val quartzDs = lookup(jndiName) as DataSource
    val sqlStr = InternalClass::class.java.getResourceAsStream("/quartz-init.sql").readBytes()
        .decodeToString()
    quartzDs.connection.use { connection ->
        connection.metaData.getTables(null, null, "%", arrayOf("TABLE")).use { result ->
            if(!result.next()) {
                //This is an empty database
                connection.createStatement().use { stmt ->
                    sqlStr.split(";").forEach {
                        stmt.executeUpdate(it)
                    }
                }
            }
        }
    }
}

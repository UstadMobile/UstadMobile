package com.ustadmobile.lib.rest

import javax.naming.InitialContext
import javax.sql.DataSource

/**
 * Initialize the Quartz Database using the resource SQL to create tables if required. The resource
 * is simply the schema as per the HypersonicSQL statements that are provided in the Quartz distribution
 */
fun InitialContext.initQuartzDb(jndiName: String) {
    val quartzDs = lookup(jndiName) as DataSource

    // Note: the class file used for the getResource call MUST be from the same module, otherwise the
    // resource will not be found by JDK9+ (due to the modular system changes)
    val sqlStr = ServerAppMain::class.java.getResourceAsStream("/quartz-init.sql").readBytes().decodeToString()
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

package com.ustadmobile.lib.rest.ext

import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import org.osjava.sj.loader.convert.SJDataSourceConverter
import java.util.*
import javax.naming.InitialContext
import javax.naming.NamingException

/**
 * Binds a datasource to java:/comp/env/jdbc/dbName if it has not already been bound
 *
 *
 */
fun InitialContext.bindDataSourceIfNotExisting(dbName: String, dbProperties: Properties) {
    val dbUrl = dbProperties.getProperty("url")
        ?: throw IllegalArgumentException("dbProperties for $dbName has no url!")
    createSubcontextIfNotExisting("java:/comp/env/jdbc")
    bindIfNotExisting("java:/comp/env/jdbc/$dbName") {
        val hostnameSanitized = sanitizeDbNameFromUrl(dbName)
        dbProperties.setProperty("url", dbUrl.replace("(hostname)", hostnameSanitized))
        SJDataSourceConverter().convert(dbProperties, "javax.sql.DataSource")
    }
    createSubcontextIfNotExisting("java:/comp/env/doordb")
    createSubcontextIfNotExisting("java:/comp/env/doordb/$dbName")
    bindIfNotExisting("java:/comp/env/doordb/$dbName/master") { true }
}

fun InitialContext.bindIfNotExisting(path: String, bindProducer: () -> Any) {
    try {
        lookup(path)
    }catch (e: NamingException) {
        bind(path, bindProducer())
    }
}

fun InitialContext.createSubcontextIfNotExisting(subcontext: String) : InitialContext{
    try {
        lookup(subcontext)
    }catch(e: NamingException) {
        createSubcontext(subcontext)
    }

    return this
}
package com.ustadmobile.lib.rest.ext

import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import org.osjava.sj.loader.convert.SJDataSourceConverter
import java.util.*
import javax.naming.InitialContext
import javax.naming.NamingException

fun InitialContext.bindHostDatabase(hostName: String, dbProperties: Properties) {
    val dbUrl = dbProperties.getProperty("url")
        ?: throw IllegalArgumentException("dbProperties for $hostName has no url!")
    createSubcontextIfNotExisting("java:/comp/env/jdbc")
    bindIfNotExisting("java:/comp/env/jdbc/$hostName") {
        val hostnameSanitized = sanitizeDbNameFromUrl(hostName)
        dbProperties.setProperty("url", dbUrl.replace("(hostname)", hostnameSanitized))
        SJDataSourceConverter().convert(dbProperties, "javax.sql.DataSource")
    }
    createSubcontextIfNotExisting("java:/comp/env/doordb")
    createSubcontextIfNotExisting("java:/comp/env/doordb/$hostName")
    bindIfNotExisting("java:/comp/env/doordb/$hostName/master") { true }
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
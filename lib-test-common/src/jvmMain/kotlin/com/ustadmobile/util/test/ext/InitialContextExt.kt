package com.ustadmobile.util.test.ext

import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import javax.naming.InitialContext

/**
 * For the given JNDI context bind an SQLite Datasource there if it is not already bound
 */
fun InitialContext.bindNewSqliteDataSourceIfNotExisting(dbName: String,
                                                        jdbcUrlPrefix: String = "jdbc:sqlite:build/tmp-",
                                                        isPrimary: Boolean = false) {
    try {
        val existingDs = lookup("java:/comp/env/jdbc/$dbName")
        return
    }catch(e: Exception) {
        //not bound yet...
    }


    val dbJndiName = "java:/comp/env/jdbc/$dbName"

    try {
        val jdbcBind = lookup("java:/comp/env/jdbc")
    }catch(e: Exception) {
        createSubcontext("java:/comp/env/jdbc")
    }

    try {
        val dataSource = this.lookup(dbJndiName)
    }catch(e: Exception) {
        val newDatasource = SQLiteDataSource(SQLiteConfig().apply{
            setJournalMode(SQLiteConfig.JournalMode.WAL)
            setBusyTimeout("30000")
            setSynchronous(SQLiteConfig.SynchronousMode.OFF)
        })

        newDatasource.url = "jdbc:sqlite:build/tmp-$dbName.sqlite"
        bind(dbJndiName, newDatasource)
    }

    if(isPrimary) {
        try {
            lookup("java:/comp/env/doordb")
        }catch (e: Exception) {
            createSubcontext("java:/comp/env/doordb")
        }

        try {
            lookup("java:/comp/env/doordb/$dbName")
        }catch (e: Exception) {
            createSubcontext("java:/comp/env/doordb/$dbName")
        }



        val primaryJndiName = "java:/comp/env/doordb/$dbName/master"

        try {
            lookup(primaryJndiName)
        }catch(e: Exception) {
            bind(primaryJndiName, isPrimary)
        }
    }
}

fun InitialContext.bindJndiForActiveEndpoint(url: String) {
    val dbNameSanitized = sanitizeDbNameFromUrl(url)
    bindNewSqliteDataSourceIfNotExisting(dbNameSanitized)
}

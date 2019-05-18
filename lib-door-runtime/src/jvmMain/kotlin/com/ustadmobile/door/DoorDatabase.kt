package com.ustadmobile.door

import java.sql.Connection
import javax.naming.InitialContext
import javax.sql.DataSource

actual abstract class DoorDatabase {

    protected lateinit var dataSource: DataSource

    var jdbcDbType: Int = -1

    var arraySupported: Boolean = false
        private set

    var context: Any? = null

    actual constructor()

    constructor(dataSource: DataSource) {
        this.dataSource = dataSource
        setupFromDataSource()
    }

    constructor(context: Any, dbName: String) {
        this.context = context
        val iContext = InitialContext()
        dataSource = iContext.lookup("java:/comp/env/jdbc/${dbName}") as DataSource
        setupFromDataSource()
    }

    protected fun setupFromDataSource() {
        var dbConnection = null as Connection?
        try{
            dbConnection = openConnection()
            jdbcDbType = DoorDbType.typeIntFromProductName(dbConnection.metaData?.databaseProductName ?: "")
            arraySupported = jdbcDbType == DoorDbType.POSTGRES
        }finally {
            dbConnection?.close()
        }
    }


    fun openConnection() = dataSource.connection

    abstract fun createAllTables()

    abstract fun clearAllTables()

}
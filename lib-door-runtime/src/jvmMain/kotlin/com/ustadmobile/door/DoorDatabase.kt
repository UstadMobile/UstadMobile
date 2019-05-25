package com.ustadmobile.door

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import javax.naming.InitialContext
import javax.sql.DataSource

actual abstract class DoorDatabase {

    protected lateinit var dataSource: DataSource

    var jdbcDbType: Int = -1

    var arraySupported: Boolean = false
        private set

    var context: Any? = null

    val tableNames: List<String> by lazy {
        var con = null as Connection?
        var tableNamesList = mutableListOf<String>()
        var tableResult = null as ResultSet?
        try {
            con = openConnection()
            val metadata = con.metaData
            tableResult = metadata.getTables(null, null, "%", arrayOf("TABLE"))
            while(tableResult.next()) {
                tableNamesList.add(tableResult.getString("TABLE_NAME"))
            }
        }finally {
            con?.close()
        }

        tableNamesList.toList()
    }

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

    actual abstract fun clearAllTables()

    companion object {
        const val DBINFO_TABLENAME = "_door_info"
    }

}
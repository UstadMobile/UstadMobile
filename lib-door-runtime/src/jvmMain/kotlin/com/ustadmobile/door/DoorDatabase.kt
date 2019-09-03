package com.ustadmobile.door

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern
import javax.naming.InitialContext
import javax.sql.DataSource

actual abstract class DoorDatabase {

    protected lateinit var dataSource: DataSource

    abstract val dbVersion: Int

    var jdbcDbType: Int = -1

    var arraySupported: Boolean = false
        private set

    var context: Any? = null

    val jdbcArraySupported by lazy {
        var connection = null as Connection?
        var sqlArray = null as java.sql.Array?
        try {
            connection = openConnection()
            sqlArray = connection?.createArrayOf("VARCHAR", arrayOf("hello"))
        }finally {
            connection?.close()
        }

        sqlArray != null
    }

    data class ChangeListenerRequest(val tableNames: List<String>, val onChange: (List<String>) -> Unit)

    val changeListeners = CopyOnWriteArrayList<ChangeListenerRequest>() as MutableList<ChangeListenerRequest>

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

    actual constructor() {

    }

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

    internal val sqlDatabaseImpl = object: DoorSqlDatabase {
        override fun execSQL(sql: String) {
            var dbConnection = null as Connection?
            var stmt = null as Statement?
            try {
                dbConnection = openConnection()
                stmt = dbConnection.createStatement()
                stmt.executeUpdate(sql)
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            } finally {
                stmt?.close()
                dbConnection?.close()
            }

        }
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

    /**
     * Postgres queries with array parameters (e.g. SELECT IN (?) need to be adjusted
     */
    fun adjustQueryWithSelectInParam(querySql: String): String {
        return if(jdbcDbType == DoorDbType.POSTGRES) {
            POSTGRES_SELECT_IN_PATTERN.matcher(querySql).replaceAll(POSTGRES_SELECT_IN_REPLACEMENT)
        }else {
            querySql
        }
    }


    fun openConnection() = dataSource.connection

    abstract fun createAllTables()

    actual abstract fun clearAllTables()

    fun addChangeListener(changeListenerRequest: ChangeListenerRequest) {
        changeListeners.add(changeListenerRequest)
    }

    fun removeChangeListener(changeListenerRequest: ChangeListenerRequest) {
        changeListeners.remove(changeListenerRequest)

    }

    fun handleTableChanged(changeTableNames: List<String>) {
        GlobalScope.launch {
            changeListeners.filter { it.tableNames.any { changeTableNames.contains(it) } }.forEach {
                it.onChange.invoke(changeTableNames)
            }
        }
    }

    companion object {
        const val DBINFO_TABLENAME = "_doorwayinfo"

        const val POSTGRES_SELECT_IN_REPLACEMENT = "IN (SELECT UNNEST(?))"

        val POSTGRES_SELECT_IN_PATTERN = Pattern.compile("IN(\\s*)\\((\\s*)\\?(\\s*)\\)",
                Pattern.CASE_INSENSITIVE)
    }

}
package com.ustadmobile.systemdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ustadmobile.systemdb.sqlite.SystemDb
import systemdb.data.LearningSpaceEntity

fun main() {
    val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    SystemDb.Schema.create(driver)
    val database = SystemDb(driver)
    database.learningSpaceQueries.insertFullObject(
        LearningSpaceEntity(1L, "http", "name", "desc", 0L, "jdbc", null, null)
    )

    val all = database.learningSpaceQueries.selectAll().executeAsList()


    println(all)



}
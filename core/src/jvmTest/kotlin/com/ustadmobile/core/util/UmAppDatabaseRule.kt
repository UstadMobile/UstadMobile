package com.ustadmobile.core.util

import com.ustadmobile.core.db.UmAppDatabase
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class UmAppDatabaseRule(private val dbName: String): TestWatcher() {

    private var dbInternal: UmAppDatabase? = null

    private var repoInternal: UmAppDatabase? = null

    val db: UmAppDatabase
        get() = dbInternal ?: throw IllegalStateException("Rule not started!")

    val repo: UmAppDatabase
        get() = repoInternal ?: throw IllegalStateException("Rule not started!")

    override fun starting(description: Description?) {

    }

    override fun finished(description: Description?) {
        super.finished(description)
    }

}
package com.ustadmobile.lib.database.annotation

/**
 * Used to define a table level index with multiple fields
 */
annotation class UmIndex(vararg val value: String, val name: String = "", val unique: Boolean = false)

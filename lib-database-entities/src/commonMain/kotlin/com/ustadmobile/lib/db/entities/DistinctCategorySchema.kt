package com.ustadmobile.lib.db.entities

class DistinctCategorySchema() {

    var contentCategoryUid: Long = 0

    var categoryName: String? = null

    var contentCategorySchemaUid: Long = 0

    var schemaName: String? = null

    override fun toString(): String {
        return this.categoryName.toString()
    }
}

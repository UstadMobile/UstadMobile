package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class DistinctCategorySchema() {

    var contentCategoryUid: Long = 0

    var categoryName: String? = null

    var contentCategorySchemaUid: Long = 0

    var schemaName: String? = null

    override fun toString(): String {
        return this.categoryName.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DistinctCategorySchema

        if (contentCategoryUid != other.contentCategoryUid) return false
        if (categoryName != other.categoryName) return false
        if (contentCategorySchemaUid != other.contentCategorySchemaUid) return false
        if (schemaName != other.schemaName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentCategoryUid.hashCode()
        result = 31 * result + (categoryName?.hashCode() ?: 0)
        result = 31 * result + contentCategorySchemaUid.hashCode()
        result = 31 * result + (schemaName?.hashCode() ?: 0)
        return result
    }


}

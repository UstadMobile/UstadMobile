package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ContentCategory.Companion.TABLE_ID
import kotlinx.serialization.Serializable


/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 * * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 * * with a dropdown list for each different schema.
 */
@Entity
@SyncableEntity(tableId = TABLE_ID)
//shortcode = ctnCat
@Serializable
class ContentCategory() {

    @PrimaryKey(autoGenerate = true)
    var contentCategoryUid: Long = 0

    var ctnCatContentCategorySchemaUid: Long = 0

    var name: String? = null

    @LocalChangeSeqNum
    var contentCategoryLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var contentCategoryMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var contentCategoryLastChangedBy: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val category = other as ContentCategory?

        if (contentCategoryUid != category!!.contentCategoryUid) return false
        if (ctnCatContentCategorySchemaUid != category.ctnCatContentCategorySchemaUid) return false
        return if (name != null) name == category.name else category.name == null
    }

    override fun hashCode(): Int {
        var result = (contentCategoryUid xor contentCategoryUid.ushr(32)).toInt()
        result = 31 * result + (ctnCatContentCategorySchemaUid xor ctnCatContentCategorySchemaUid.ushr(32)).toInt()
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 1
    }
}

package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum
import com.ustadmobile.lib.db.entities.ContentCategory.Companion.TABLE_ID


/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 * * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 * * with a dropdown list for each different schema.
 */
@UmEntity(tableId = TABLE_ID)
@Entity
//shortcode = ctnCat
class ContentCategory {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var contentCategoryUid: Long = 0

    var ctnCatContentCategorySchemaUid: Long = 0

    var name: String? = null

    @UmSyncLocalChangeSeqNum
    var contentCategoryLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var contentCategoryMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
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

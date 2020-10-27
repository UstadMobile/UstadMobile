package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Category.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class Category() {

    @PrimaryKey(autoGenerate = true)
    var categoryUid: Long = 0

    //Name eg: Blue ribbon
    var categoryName: String? = null

    var categoryNameDari: String? = null

    var categoryDescDari: String? = null

    var categoryNamePashto: String? = null

    var categoryDescPashto: String? = null

    //Description eg: A blue ribbon used for gift wrapping.
    var categoryDesc: String? = null

    //Date added in unix datetime
    var categoryDateAdded: Long = 0

    //Person who added this category (person uid)
    var categoryPersonAdded: Long = 0

    //Picture uid <-> categoryPicture 's pk
    var categoryPictureUid: Long = 0

    //If the category active . False is effectively delete
    var categoryActive: Boolean = false

    @MasterChangeSeqNum
    var categoryMCSN: Long = 0

    @LocalChangeSeqNum
    var categoryLCSN: Long = 0

    @LastChangedBy
    var categoryLCB: Int = 0

    fun getNameLocale(locale: String): String{
        var categoryNameLocale : String?
        if(locale.equals("fa")){
            categoryNameLocale = categoryNameDari
        }else if(locale.equals("ps")){
            categoryNameLocale = categoryNamePashto
        }else{
            categoryNameLocale = categoryName
        }
        if(categoryNameLocale.equals("")){
            categoryNameLocale = categoryName
        }
        if(categoryNameLocale == null ){
            categoryNameLocale = ""
        }
        if(categoryNameLocale.isEmpty() && categoryName != null &&
                categoryName!!.isNotEmpty()){
            categoryNameLocale = categoryName
        }
        if(categoryNameLocale != null && categoryNameLocale.isEmpty() &&
                categoryNameDari != null &&
                categoryNameDari!!.isNotEmpty()) {
            categoryNameLocale = categoryNameDari
        }
        if(categoryNameLocale != null && categoryNameLocale.isEmpty() &&
                categoryNamePashto != null &&
                categoryNamePashto!!.isNotEmpty()) {
            categoryNameLocale = categoryNamePashto
        }
        if(categoryNameLocale == null ){
            categoryNameLocale = ""
        }
        return categoryNameLocale
    }

    fun getDescLocale(locale: String): String{
        var categoryDescLocale : String?
        if(locale.equals("fa")){
            categoryDescLocale = categoryDescDari
        }else if(locale.equals("ps")){
            categoryDescLocale = categoryDescPashto
        }else{
            categoryDescLocale = categoryDesc
        }
        if(categoryDescLocale.equals("")){
            categoryDescLocale = categoryDesc
        }
        if(categoryDescLocale == null ){
            categoryDescLocale = ""
        }

        if(categoryDescLocale.isEmpty() && categoryDesc != null &&
                categoryName!!.isNotEmpty()){
            categoryDescLocale = categoryDesc
        }
        if(categoryDescLocale != null && categoryDescLocale.isEmpty() &&
                categoryDescDari != null &&
                categoryDescDari!!.isNotEmpty()) {
            categoryDescLocale = categoryDescDari
        }
        if(categoryDescLocale != null && categoryDescLocale.isEmpty() &&
                categoryDescPashto != null &&
                categoryDescPashto!!.isNotEmpty()) {
            categoryDescLocale = categoryDescPashto
        }
        if(categoryDescLocale == null ){
            categoryDescLocale = ""
        }

        return categoryDescLocale
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Category

        if (categoryUid != other.categoryUid) return false
        if (categoryName != other.categoryName) return false
        if (categoryNameDari != other.categoryNameDari) return false
        if (categoryDescDari != other.categoryDescDari) return false
        if (categoryNamePashto != other.categoryNamePashto) return false
        if (categoryDescPashto != other.categoryDescPashto) return false
        if (categoryDesc != other.categoryDesc) return false
        if (categoryDateAdded != other.categoryDateAdded) return false
        if (categoryPersonAdded != other.categoryPersonAdded) return false
        if (categoryPictureUid != other.categoryPictureUid) return false
        if (categoryActive != other.categoryActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categoryUid.hashCode()
        result = 31 * result + (categoryName?.hashCode() ?: 0)
        result = 31 * result + (categoryNameDari?.hashCode() ?: 0)
        result = 31 * result + (categoryDescDari?.hashCode() ?: 0)
        result = 31 * result + (categoryNamePashto?.hashCode() ?: 0)
        result = 31 * result + (categoryDescPashto?.hashCode() ?: 0)
        result = 31 * result + (categoryDesc?.hashCode() ?: 0)
        result = 31 * result + categoryDateAdded.hashCode()
        result = 31 * result + categoryPersonAdded.hashCode()
        result = 31 * result + categoryPictureUid.hashCode()
        result = 31 * result + categoryActive.hashCode()
        return result
    }

    companion object{
        const val CATEGORY_TABLE_ID = 211
    }


}
package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SaleProduct.Companion.SALE_PRODUCT_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = SALE_PRODUCT_TABLE_ID)
@Serializable
open class SaleProduct() {



    @PrimaryKey(autoGenerate = true)
    var saleProductUid: Long = 0


    //Name eg: Blue ribbon
    var saleProductName: String? = null

    var saleProductNameDari: String? = null

    var saleProductDescDari: String? = null

    var saleProductNamePashto: String? = null

    var saleProductDescPashto: String? = null

    //Description eg: A blue ribbon used for gift wrapping.
    var saleProductDesc: String? = null

    //Date added in unix datetime
    var saleProductDateAdded: Long = 0

    //Person who added this product (person uid)
    var saleProductPersonAdded: Long = 0

    //Picture uid <-> SaleProductPicture 's pk
    var saleProductPictureUid: Long = 0

    //If the product active . False is effectively delete
    var saleProductActive: Boolean = false

    //If it is a category it is true. If it is a product it is false (default)
    var saleProductCategory: Boolean = false

    //Base price. Could be 4242.42
    var saleProductBasePrice : Float = 0F

    @MasterChangeSeqNum
    var saleProductMCSN: Long = 0

    @LocalChangeSeqNum
    var saleProductLCSN: Long = 0

    @LastChangedBy
    var saleProductLCB: Int = 0

    init {
        this.saleProductCategory = false
    }

    constructor(name: String, decs: String) : this() {
        this.saleProductName = name
        this.saleProductDesc = decs
        this.saleProductActive = true
        this.saleProductCategory = false

    }

    constructor(name: String, decs: String, category: Boolean) : this() {
        this.saleProductName = name
        this.saleProductDesc = decs
        this.saleProductActive = true
        this.saleProductCategory = category

    }

    constructor(name: String, decs: String, category: Boolean, isActive: Boolean) : this() {
        this.saleProductName = name
        this.saleProductDesc = decs
        this.saleProductActive = isActive
        this.saleProductCategory = category
    }

    fun getNameLocale(locale: String): String{
        var saleProductNameLocale : String?
        if(locale.equals("fa")){
            saleProductNameLocale = saleProductNameDari
        }else if(locale.equals("ps")){
            saleProductNameLocale = saleProductNamePashto
        }else{
            saleProductNameLocale = saleProductName
        }
        if(saleProductNameLocale.equals("")){
            saleProductNameLocale = saleProductName
        }
        if(saleProductNameLocale == null ){
            saleProductNameLocale = ""
        }
        if(saleProductNameLocale.isEmpty() && saleProductName != null &&
                saleProductName!!.isNotEmpty()){
            saleProductNameLocale = saleProductName
        }
        if(saleProductNameLocale != null && saleProductNameLocale.isEmpty() &&
                saleProductNameDari != null &&
                saleProductNameDari!!.isNotEmpty()) {
            saleProductNameLocale = saleProductNameDari
        }
        if(saleProductNameLocale != null && saleProductNameLocale.isEmpty() &&
                saleProductNamePashto != null &&
                saleProductNamePashto!!.isNotEmpty()) {
            saleProductNameLocale = saleProductNamePashto
        }
        if(saleProductNameLocale == null ){
            saleProductNameLocale = ""
        }
        return saleProductNameLocale
    }

    fun getDescLocale(locale: String): String{
        var saleProductDescLocale : String?
        if(locale.equals("fa")){
            saleProductDescLocale = saleProductDescDari
        }else if(locale.equals("ps")){
            saleProductDescLocale = saleProductDescPashto
        }else{
            saleProductDescLocale = saleProductDesc
        }
        if(saleProductDescLocale.equals("")){
            saleProductDescLocale = saleProductDesc
        }
        if(saleProductDescLocale == null ){
            saleProductDescLocale = ""
        }

        if(saleProductDescLocale.isEmpty() && saleProductDesc != null &&
                saleProductName!!.isNotEmpty()){
            saleProductDescLocale = saleProductDesc
        }
        if(saleProductDescLocale != null && saleProductDescLocale.isEmpty() &&
                saleProductDescDari != null &&
                saleProductDescDari!!.isNotEmpty()) {
            saleProductDescLocale = saleProductDescDari
        }
        if(saleProductDescLocale != null && saleProductDescLocale.isEmpty() &&
                saleProductDescPashto != null &&
                saleProductDescPashto!!.isNotEmpty()) {
            saleProductDescLocale = saleProductDescPashto
        }
        if(saleProductDescLocale == null ){
            saleProductDescLocale = ""
        }

        return saleProductDescLocale
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleProduct

        if (saleProductUid != other.saleProductUid) return false
        if (saleProductName != other.saleProductName) return false
        if (saleProductNameDari != other.saleProductNameDari) return false
        if (saleProductDescDari != other.saleProductDescDari) return false
        if (saleProductNamePashto != other.saleProductNamePashto) return false
        if (saleProductDescPashto != other.saleProductDescPashto) return false
        if (saleProductDesc != other.saleProductDesc) return false
        if (saleProductDateAdded != other.saleProductDateAdded) return false
        if (saleProductPersonAdded != other.saleProductPersonAdded) return false
        if (saleProductPictureUid != other.saleProductPictureUid) return false
        if (saleProductActive != other.saleProductActive) return false
        if (saleProductCategory != other.saleProductCategory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = saleProductUid.hashCode()
        result = 31 * result + (saleProductName?.hashCode() ?: 0)
        result = 31 * result + (saleProductNameDari?.hashCode() ?: 0)
        result = 31 * result + (saleProductDescDari?.hashCode() ?: 0)
        result = 31 * result + (saleProductNamePashto?.hashCode() ?: 0)
        result = 31 * result + (saleProductDescPashto?.hashCode() ?: 0)
        result = 31 * result + (saleProductDesc?.hashCode() ?: 0)
        result = 31 * result + saleProductDateAdded.hashCode()
        result = 31 * result + saleProductPersonAdded.hashCode()
        result = 31 * result + saleProductPictureUid.hashCode()
        result = 31 * result + saleProductActive.hashCode()
        result = 31 * result + saleProductCategory.hashCode()
        return result
    }

    companion object{
        const val SALE_PRODUCT_TABLE_ID = 204
    }


}
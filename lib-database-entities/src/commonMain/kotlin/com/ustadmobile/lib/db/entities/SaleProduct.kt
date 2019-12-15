package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 67)
@Entity
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
        return saleProductDescLocale
    }
}

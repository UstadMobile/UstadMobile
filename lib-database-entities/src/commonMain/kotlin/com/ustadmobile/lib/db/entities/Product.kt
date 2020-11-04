package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Product.Companion.PRODUCT_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = PRODUCT_TABLE_ID)
@Serializable
open class Product() {

    @PrimaryKey(autoGenerate = true)
    var productUid: Long = 0

    //Name eg: Blue ribbon
    var productName: String? = null

    var productNameDari: String? = null

    var productDescDari: String? = null

    var productNamePashto: String? = null

    var productDescPashto: String? = null

    //Description eg: A blue ribbon used for gift wrapping.
    var productDesc: String? = null

    //Date added in unix datetime
    var productDateAdded: Long = 0

    //Person who added this product (person uid)
    var productPersonAdded: Long = 0

    //Picture uid <-> productPicture 's pk
    var productPictureUid: Long = 0

    //If the product active . False is effectively delete
    var productActive: Boolean = true

    //Base price. Could be 4242.42
    var productBasePrice : Float = 0F

    @MasterChangeSeqNum
    var productMCSN: Long = 0

    @LocalChangeSeqNum
    var productLCSN: Long = 0

    @LastChangedBy
    var productLCB: Int = 0


    constructor(name: String, decs: String) : this() {
        this.productName = name
        this.productDesc = decs
        this.productActive = true

    }

    fun getNameLocale(locale: String): String{
        var productNameLocale : String?
        if(locale.equals("fa")){
            productNameLocale = productNameDari
        }else if(locale.equals("ps")){
            productNameLocale = productNamePashto
        }else{
            productNameLocale = productName
        }
        if(productNameLocale.equals("")){
            productNameLocale = productName
        }
        if(productNameLocale == null ){
            productNameLocale = ""
        }
        if(productNameLocale.isEmpty() && productName != null &&
                productName!!.isNotEmpty()){
            productNameLocale = productName
        }
        if(productNameLocale != null && productNameLocale.isEmpty() &&
                productNameDari != null &&
                productNameDari!!.isNotEmpty()) {
            productNameLocale = productNameDari
        }
        if(productNameLocale != null && productNameLocale.isEmpty() &&
                productNamePashto != null &&
                productNamePashto!!.isNotEmpty()) {
            productNameLocale = productNamePashto
        }
        if(productNameLocale == null ){
            productNameLocale = ""
        }
        return productNameLocale
    }

    fun getDescLocale(locale: String): String{
        var productDescLocale : String?
        if(locale.equals("fa")){
            productDescLocale = productDescDari
        }else if(locale.equals("ps")){
            productDescLocale = productDescPashto
        }else{
            productDescLocale = productDesc
        }
        if(productDescLocale.equals("")){
            productDescLocale = productDesc
        }
        if(productDescLocale == null ){
            productDescLocale = ""
        }

        if(productDescLocale.isEmpty() && productDesc != null &&
                productName!!.isNotEmpty()){
            productDescLocale = productDesc
        }
        if(productDescLocale != null && productDescLocale.isEmpty() &&
                productDescDari != null &&
                productDescDari!!.isNotEmpty()) {
            productDescLocale = productDescDari
        }
        if(productDescLocale != null && productDescLocale.isEmpty() &&
                productDescPashto != null &&
                productDescPashto!!.isNotEmpty()) {
            productDescLocale = productDescPashto
        }
        if(productDescLocale == null ){
            productDescLocale = ""
        }

        return productDescLocale
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Product

        if (productUid != other.productUid) return false
        if (productName != other.productName) return false
        if (productNameDari != other.productNameDari) return false
        if (productDescDari != other.productDescDari) return false
        if (productNamePashto != other.productNamePashto) return false
        if (productDescPashto != other.productDescPashto) return false
        if (productDesc != other.productDesc) return false
        if (productDateAdded != other.productDateAdded) return false
        if (productPersonAdded != other.productPersonAdded) return false
        if (productPictureUid != other.productPictureUid) return false
        if (productActive != other.productActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productUid.hashCode()
        result = 31 * result + (productName?.hashCode() ?: 0)
        result = 31 * result + (productNameDari?.hashCode() ?: 0)
        result = 31 * result + (productDescDari?.hashCode() ?: 0)
        result = 31 * result + (productNamePashto?.hashCode() ?: 0)
        result = 31 * result + (productDescPashto?.hashCode() ?: 0)
        result = 31 * result + (productDesc?.hashCode() ?: 0)
        result = 31 * result + productDateAdded.hashCode()
        result = 31 * result + productPersonAdded.hashCode()
        result = 31 * result + productPictureUid.hashCode()
        result = 31 * result + productActive.hashCode()
        return result
    }

    companion object{
        const val PRODUCT_TABLE_ID = 213
    }


}
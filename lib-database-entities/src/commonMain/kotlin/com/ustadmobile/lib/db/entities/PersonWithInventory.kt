package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable


/**
 * Person 's POJO for representing a WE with inventory count
 */
@Serializable
class PersonWithInventory() : Person() {

    //Total inventory count
    var inventoryCount = -1

    var inventoryCountDeliveredTotal = -1

    var inventoryCountDelivered = -1

    //Inventory selected
    var inventorySelected = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as PersonWithInventory

        if (inventoryCount != other.inventoryCount) return false
        if (inventoryCountDeliveredTotal != other.inventoryCountDeliveredTotal) return false
        if (inventoryCountDelivered != other.inventoryCountDelivered) return false
        if (inventorySelected != other.inventorySelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + inventoryCount
        result = 31 * result + inventoryCountDeliveredTotal
        result = 31 * result + inventoryCountDelivered
        result = 31 * result + inventorySelected
        return result
    }


}
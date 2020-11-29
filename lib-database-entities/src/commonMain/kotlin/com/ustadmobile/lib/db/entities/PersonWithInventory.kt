package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable


/**
 * Person 's POJO for representing a WE with inventory count
 */
@Serializable
class PersonWithInventory() : Person() {

    //Total inventory count
    var inventoryCount = 0

    var inventoryCountDeliveredTotal = 0

    var inventoryCountDelivered = 0

    //Inventory selected
    var inventorySelected = 0


}
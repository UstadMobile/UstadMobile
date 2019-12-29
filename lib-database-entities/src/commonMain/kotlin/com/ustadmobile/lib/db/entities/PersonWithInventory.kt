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


}

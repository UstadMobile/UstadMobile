package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable


/**
 * Person 's POJO for representing a WE with inventory count
 */
@Serializable
class PersonWithInventory() : Person() {

        var inventoryCount = 0

}

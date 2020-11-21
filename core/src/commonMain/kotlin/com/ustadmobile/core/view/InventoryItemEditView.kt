package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.PersonWithInventory


interface InventoryItemEditView: UstadEditView<InventoryItem> {

    //The list of producers / women embroiderers to be set on the view
    //var womenEmbroiderers : List<PersonWithInventory>

    var producers: DoorMutableLiveData<List<PersonWithInventory>>?

    companion object {

        const val VIEW_NAME = "InventoryItemEditEditView"

    }

}
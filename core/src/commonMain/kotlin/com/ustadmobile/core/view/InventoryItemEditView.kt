package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock


interface InventoryItemEditView: UstadEditView<InventoryItem> {

    var producers: DoorMutableLiveData<List<PersonWithInventoryItemAndStock>>?

    companion object {

        const val VIEW_NAME = "InventoryItemEditEditView"

    }

}
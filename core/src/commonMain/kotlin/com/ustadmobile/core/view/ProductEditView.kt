package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product


interface ProductEditView: UstadEditView<Product> {

    var categories: DoorMutableLiveData<List<Category>>?

    companion object {

        const val VIEW_NAME = "ProductEditEditView"

    }

}
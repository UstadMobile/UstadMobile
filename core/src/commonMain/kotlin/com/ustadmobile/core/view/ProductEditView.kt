package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductPicture


interface ProductEditView: UstadEditView<Product> {

    var productPicture: ProductPicture?
    
    var productPicturePath: String?

    var categories: DoorMutableLiveData<List<Category>>?

    companion object {

        const val VIEW_NAME = "ProductEditEditView"

    }

}
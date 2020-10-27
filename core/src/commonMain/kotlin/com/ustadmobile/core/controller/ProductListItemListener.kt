package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

interface ProductListItemListener {

    fun onClickProduct(product: ProductWithInventoryCount)

}
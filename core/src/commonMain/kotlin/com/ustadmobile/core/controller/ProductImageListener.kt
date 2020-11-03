package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Product

interface ProductImageListener {

    fun onClickEntry(product: Product)

}
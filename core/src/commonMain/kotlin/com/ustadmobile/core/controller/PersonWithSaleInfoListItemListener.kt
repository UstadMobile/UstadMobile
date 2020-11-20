package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.PersonWithSaleInfo


interface PersonWithSaleInfoListItemListener {

    fun onClickPersonWithSaleInfo(personWithSaleInfo: PersonWithSaleInfo)

}
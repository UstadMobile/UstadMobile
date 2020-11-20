package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo


class DefaultPersonWithSaleInfoListItemListener(var view: PersonWithSaleInfoListView?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): PersonWithSaleInfoListItemListener {

    override fun onClickPersonWithSaleInfo(personWithSaleInfo: PersonWithSaleInfo) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(PersonDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to personWithSaleInfo.personUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(personWithSaleInfo))
        }
    }
}

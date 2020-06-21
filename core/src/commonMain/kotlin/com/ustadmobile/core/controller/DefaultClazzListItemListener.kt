package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz

class DefaultClazzListItemListener(var view: ClazzList2View?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): ClazzListItemListener {

    override fun onClickClazz(clazz: Clazz) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ClazzDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to clazz.clazzUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(clazz))
        }
    }
}
package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ScopedGrantListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class DefaultScopedGrantListItemListener(
    var view: ScopedGrantListView?,
    var listViewMode: ListViewMode,
    val context: Any,
    override val di: DI
): ScopedGrantListItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onClickScopedGrant(scopedGrant: ScopedGrantWithName) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(ScopedGrantDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to scopedGrant.sgUid.toString()), context)
        }

        //Picker is not used for ScopedGrant
    }
}

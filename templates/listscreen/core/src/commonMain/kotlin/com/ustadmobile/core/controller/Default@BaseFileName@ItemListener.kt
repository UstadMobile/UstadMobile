package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.@Entity@DetailView
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class Default@BaseFileName@ItemListener(
    var view: @BaseFileName@View?,
    var listViewMode: ListViewMode,
    val context: Any,
    override val di: DI
): @BaseFileName@ItemListener, DIAware {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onClick@Entity@(@Entity_VariableName@: @DisplayEntity@) {
        if(listViewMode == ListViewMode.BROWSER) {
            systemImpl.go(@Entity@DetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to @Entity_VariableName@.@Entity_VariableName@Uid.toString()), context)
        }else {
            view?.finishWithResult(listOf(@Entity_VariableName@))
        }
    }
}

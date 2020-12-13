package com.ustadmobile.core.controller

import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ReportEditFilterView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ReportFilter
import org.kodein.di.DI


class ReportEditFilterPresenter(context: Any,
                                arguments: Map<String, String>, view: ReportEditFilterView,
                                di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportEditFilterView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        super.onLoadFromJson(bundle)

        return null
    }

    // based on enum selection
    fun handleFieldOptionSelected(fieldOption: String){
        // changes the drop down option of condition and values
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, null, entityVal)
    }

    override fun handleClickSave(entity: ReportFilter) {
        TODO("Not yet implemented")
    }


}
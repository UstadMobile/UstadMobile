package com.ustadmobile.core.view

import com.ustadmobile.core.controller.DateRangePresenter
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment


interface DateRangeView: UstadEditView<DateRangeMoment> {

    var relUnitOptions : List<DateRangePresenter.RelUnitMessageIdOption>?

    var relToOptions : List<DateRangePresenter.RelToMessageIdOption>?

    var fromFixedDateMissing: String?

    var toFixedDateMissing: String?

    companion object {

        const val VIEW_NAME = "DateRangeEditView"

    }

}
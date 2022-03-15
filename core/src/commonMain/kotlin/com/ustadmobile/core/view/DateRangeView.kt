package com.ustadmobile.core.view

import com.ustadmobile.core.controller.DateRangePresenter
import com.ustadmobile.lib.db.entities.DateRangeMoment


interface DateRangeView: UstadEditView<DateRangeMoment> {

    var relUnitOptions : List<DateRangePresenter.RelUnitMessageIdOption>?

    var relToOptions : List<DateRangePresenter.RelToMessageIdOption>?

    var fromFixedDateMissing: String?

    var toFixedDateMissing: String?

    var toRelativeDateInvalid: String?

    companion object {

        const val VIEW_NAME = "DateRangeEditView"

    }

}
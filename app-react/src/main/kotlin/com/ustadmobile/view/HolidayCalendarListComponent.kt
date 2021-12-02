package com.ustadmobile.view

import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.createItemWithIconTitleAndDescription
import react.RBuilder

class HolidayCalendarListComponent(mProps: UmProps): UstadListComponent<HolidayCalendar, HolidayCalendarWithNumEntries>(mProps),
    HolidayCalendarListView {

    override val viewName: String
        get() = HolidayCalendarListView.VIEW_NAME

    private var mPresenter: HolidayCalendarListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.holidayCalendarDao

    override val listPresenter: UstadListPresenter<*, in HolidayCalendarWithNumEntries>?
        get() = mPresenter


    override fun onCreateView() {
        super.onCreateView()
        showCreateNewItem = true
        createNewText = getString(MessageID.add_a_new_holiday_calendar)
        mPresenter = HolidayCalendarListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: HolidayCalendarWithNumEntries) {
        val titleText = getString(MessageID.num_items_with_name)
            .format(item.numEntries, getString(MessageID.holidays))
        createItemWithIconTitleAndDescription("calendar_today",
            item.umCalendarName, titleText
        )
    }

    override fun handleClickEntry(entry: HolidayCalendarWithNumEntries) {
        mPresenter?.handleClickEntry(entry as HolidayCalendar)
    }
}
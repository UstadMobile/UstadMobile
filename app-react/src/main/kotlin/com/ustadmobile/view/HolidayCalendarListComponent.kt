package com.ustadmobile.view

import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.createItemWithIconTitleAndDescription
import react.RBuilder
import react.RProps

class HolidayCalendarListComponent(mProps: RProps): UstadListComponent<HolidayCalendar, HolidayCalendarWithNumEntries>(mProps),
    HolidayCalendarListView {

    private var mPresenter: HolidayCalendarListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.holidayCalendarDao

    override val listPresenter: UstadListPresenter<*, in HolidayCalendarWithNumEntries>?
        get() = mPresenter


    override fun onCreateView() {
        super.onCreateView()
        showCreateNewItem = true
        createNewTextId = MessageID.add_a_new_holiday_calendar
        mPresenter = HolidayCalendarListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: HolidayCalendarWithNumEntries) {
        createItemWithIconTitleAndDescription("calendar_today", item.umCalendarName,
            getString(MessageID.num_items_with_name)
                .format(item.numEntries, getString(MessageID.holidays))
        )
    }

    override val viewName: String
        get() = HolidayCalendarListView.VIEW_NAME

    override fun handleClickEntry(entry: HolidayCalendarWithNumEntries) {
        mPresenter?.handleClickEntry(entry as HolidayCalendar)
    }
}
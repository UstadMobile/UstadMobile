package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import react.RBuilder
import react.setState

class HolidayCalendarEditComponent(mProps: UmProps): UstadEditComponent<HolidayCalendar>(mProps),
    HolidayCalendarEditView{

    private var mPresenter: HolidayCalendarEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, HolidayCalendar>?
        get() = mPresenter

    override val viewName: String
        get() = HolidayCalendarEditView.VIEW_NAME

    private var holidays: List<Holiday>? = null

    private val holidayObserver = ObserverFnWrapper<List<Holiday>?> {
        setState {
            holidays = it
        }
    }

    override var holidayList: DoorLiveData<List<Holiday>>? = null
        get() = field
        set(value) {
            field = value
            field?.removeObserver(holidayObserver)
            value?.observe(this, holidayObserver)
        }

    private val holidayLabel = FieldLabel(getString(MessageID.name))

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var entity: HolidayCalendar? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()

        mPresenter = HolidayCalendarEditPresenter(this, arguments, this, this,di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        setEditTitle(MessageID.add_a_holiday, MessageID.edit_holiday)
    }

    override fun RBuilder.render() {
      /*  styledDiv {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer(MGridSpacing.spacing4) {
                css{
                    padding(2.spacingUnits, 2.spacingUnits,0.spacingUnits)
                }
                umItem(MGridSize.cells12) {
                    mTextField(label = "${holidayLabel.text}",
                        helperText = holidayLabel.errorText,
                        value = entity?.umCalendarName, error = holidayLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.umCalendarName = it.targetInputValue
                            }
                        }) {
                        css(defaultFullWidth)
                    }
                }
            }

            val newItem = CreateNewItem(true,MessageID.add_a_holiday){
                mPresenter?.holidayToManyJoinListener?.onClickEdit(Holiday())
            }

            holidays?.let { holidays ->
                renderHolidays(holidays, newItem){ holiday ->
                    mPresenter?.holidayToManyJoinListener?.onClickEdit(holiday)
                }
            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}


class HolidayListComponent(mProps: ListProps<Holiday>): UstadSimpleList<ListProps<Holiday>>(mProps){
    override fun RBuilder.renderListItem(item: Holiday) {
        //createItemWithIconTitleAndDescription("date_range",item.holName, "${Date(item.holStartTime).standardFormat()} - ${Date(item.holEndTime).standardFormat()}")
    }
}

fun RBuilder.renderHolidays(holidays: List<Holiday>,
                            createNewItem: CreateNewItem = CreateNewItem(),
                            onEntryClicked: ((Holiday) -> Unit)? = null) = child(HolidayListComponent::class) {
    attrs.entries = holidays
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.mainList = true
}
package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.ext.observeResult
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.padding
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class HolidayCalendarEditComponent(mProps: RProps): UstadEditComponent<HolidayCalendar>(mProps),
    HolidayCalendarEditView{

    private var mPresenter: HolidayCalendarEditPresenter? = null

    private val serializer = Holiday.serializer()

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
            console.log(value)
        }

    override fun onCreate() {
        super.onCreate()

        mPresenter = HolidayCalendarEditPresenter(this, arguments, this, this,di)
        mPresenter?.onCreate(mapOf())

        setEditTitle(MessageID.add_a_holiday, MessageID.edit_holiday)

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
            serializer) {
            val holiday = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditHoliday(holiday)
        }
    }

    override fun RBuilder.render() {
        styledDiv {
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
                onClickEditHoliday(null)
            }

            holidays?.let { holidays ->
                renderHolidays(holidays, newItem){ holiday ->
                    onClickEditHoliday(holiday)
                }
            }
        }
    }

    private fun onClickEditHoliday(holiday: Holiday?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(holiday, HolidayEditView.VIEW_NAME,
            navOptions = DEFAULT_NAV_OPTION(serializer))
    }
}


class HolidayListComponent(mProps: ListProps<Holiday>): UstadSimpleList<ListProps<Holiday>>(mProps){
    override fun RBuilder.renderListItem(item: Holiday) {

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
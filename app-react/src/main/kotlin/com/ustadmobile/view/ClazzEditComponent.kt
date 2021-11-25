package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import react.RBuilder
import com.ustadmobile.util.*
import react.setState

class ClazzEditComponent (mProps: UmProps): UstadEditComponent<ClazzWithHolidayCalendarAndSchool>(mProps),
    ClazzEdit2View {

    private var mPresenter: ClazzEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWithHolidayCalendarAndSchool>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzEdit2View.VIEW_NAME

    private var clazzNameLabel = FieldLabel(text = getString(MessageID.class_name))

    private var clazzDescLabel = FieldLabel(text = getString(MessageID.class_description))

    private var startDateLabel = FieldLabel(text = getString(MessageID.start_date))

    private var endDateLabel = FieldLabel(text = getString(MessageID.end_date))

    private var timeZoneLabel = FieldLabel(text = getString(MessageID.timezone))

    private var holidayCalenderLabel = FieldLabel(text = getString(MessageID.holiday_calendar))

    private var schoolNameLabel = FieldLabel(text = getString(MessageID.school))

    private var featureLabel = FieldLabel(text = getString(MessageID.features_enabled))

    private var scheduleList: List<Schedule>? = null

    private val scheduleObserver = ObserverFnWrapper<List<Schedule>?> {
            setState {
                scheduleList = it
            }
    }

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        get() = field
        set(value) {
            field?.removeObserver(scheduleObserver)
            field = value
            value?.observe(this, scheduleObserver)
        }

    override var clazzEndDateError: String? = null
        set(value) {
            setState {
                endDateLabel = endDateLabel.copy(errorText = value)
            }
        }

    override var clazzStartDateError: String? = null
        set(value) {
            setState {
                startDateLabel = startDateLabel.copy(errorText = value)
            }
        }

    private var scopeList: List<ScopedGrantAndName>? = null

    private val scopedGrantListObserver = ObserverFnWrapper<List<ScopedGrantAndName>> {
        setState {
            scopeList = it
        }
    }

    override var scopedGrants: DoorLiveData<List<ScopedGrantAndName>>? = null
        set(value) {
            field?.removeObserver(scopedGrantListObserver)
            field = value
            field?.observe(this, scopedGrantListObserver)
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzWithHolidayCalendarAndSchool? = null
        get() = field
        set(value) {
            if(value?.clazzName != null){
                title = value.clazzName
            }

            setState{
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.add_a_new_class, MessageID.edit_clazz)
        mPresenter = ClazzEdit2Presenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
       /* styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }
            umGridContainer(MGridSpacing.spacing4) {
                umItem(MGridSize.cells12, MGridSize.cells4){
                    umEntityAvatar(fallbackSrc = ASSET_ENTRY, listItem = true)
                }

                umItem(MGridSize.cells12, MGridSize.cells8){

                    createListSectionTitle(getString(MessageID.basic_details))

                    mTextField(label = "${clazzNameLabel.text}",
                        helperText = clazzNameLabel.errorText,
                        value = entity?.clazzName,
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.clazzName = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }


                    mTextField(label = "${clazzDescLabel.text}",
                        value = entity?.clazzDesc,
                        error = clazzDescLabel.error,
                        disabled = !fieldsEnabled,
                        helperText = clazzDescLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.clazzDesc = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mDateTimePicker(
                                label = "${startDateLabel.text}",
                                ruleSet = defaultFullWidth,
                                error = startDateLabel.error,
                                helperText = startDateLabel.errorText,
                                value = entity?.clazzStartTime.toDate(),
                                inputVariant = MFormControlVariant.outlined,
                                pickerType = MDateTimePickerType.date,
                                onChange = { mills, _ ->
                                    setState {
                                        entity?.clazzStartTime = mills
                                        clazzStartDateError = null
                                    }
                                })
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {

                            mDateTimePicker(
                                label = "${endDateLabel.text}",
                                ruleSet = defaultFullWidth,
                                error = endDateLabel.error,
                                helperText = endDateLabel.errorText,
                                value = entity?.clazzEndTime.toDate(),
                                inputVariant = MFormControlVariant.outlined,
                                pickerType = MDateTimePickerType.date,
                                onChange = { mills, utc ->
                                    setState {
                                        entity?.clazzEndTime = mills
                                        clazzEndDateError = null
                                    }
                                })
                        }
                    }

                    createListSectionTitle(getString(MessageID.schedule))

                    val createNewItem = CreateNewItem(true, MessageID.add_a_schedule){
                        mPresenter?.scheduleOneToManyJoinListener?.onClickNew()
                    }

                    mPresenter?.let { presenter ->
                        scheduleList?.let { schedules ->
                            renderSchedules(presenter.scheduleOneToManyJoinListener, schedules, createNewItem){
                                mPresenter?.scheduleOneToManyJoinListener?.onClickEdit(it)
                            }
                        }
                    }

                    mSpacer()

                    mTextField(label = "${schoolNameLabel.text}",
                        helperText = schoolNameLabel.errorText,
                        value = entity?.school?.schoolName,
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {}
                        }){
                        attrs.asDynamic().onClick = {
                            mPresenter?.handleClickSchool()
                        }
                        css(defaultFullWidth)
                    }


                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${timeZoneLabel.text}",
                                value = entity?.clazzTimeZone,
                                error = timeZoneLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = timeZoneLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.clazzTimeZone = it.targetInputValue
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleClickTimezone()
                                }
                                css(defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${holidayCalenderLabel.text}",
                                value = entity?.holidayCalendar?.umCalendarName,
                                error = holidayCalenderLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = holidayCalenderLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        clazzEndDateError = null
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleHolidayCalendarClicked()
                                }
                                css(defaultFullWidth)
                            }
                        }
                    }

                    mTextField(label = "${featureLabel.text}",
                        helperText = featureLabel.errorText,
                        value = setBitmaskListText(systemImpl,entity?.clazzFeatures),
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.clazzFeatures = it.targetInputValue.toLong()
                            }
                        }){
                        attrs.asDynamic().onClick = {
                            mPresenter?.handleClickFeatures()
                        }
                        css(defaultFullWidth)
                    }

                    createListSectionTitle(getString(MessageID.permissions))

                    mPresenter?.let { presenter ->
                        scopeList?.let { scopeList ->

                            val newItem = CreateNewItem(true, MessageID.add_person_or_group){
                                mPresenter?.scopedGrantOneToManyHelper?.onClickNew()
                            }

                            renderScopedGrants(presenter.scopedGrantOneToManyHelper, scopeList.distinctBy { it.name },
                                newItem){ scope ->
                                mPresenter?.scopedGrantOneToManyHelper?.onClickEdit(scope)
                            }
                        }
                    }

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
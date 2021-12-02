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
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

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

    private var scheduleList: List<Schedule> = listOf()

    private val scheduleObserver = ObserverFnWrapper<List<Schedule>?> {
        if(it.isNullOrEmpty()) return@ObserverFnWrapper
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
        styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }
            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells4){
                    umEntityAvatar(fallbackSrc = ASSET_ENTRY, listItem = true)
                }

                umItem(GridSize.cells12, GridSize.cells8){

                    createListSectionTitle(getString(MessageID.basic_details))

                    umTextField(label = "${clazzNameLabel.text}",
                        helperText = clazzNameLabel.errorText,
                        value = entity?.clazzName,
                        error = clazzNameLabel.error,
                        fullWidth = true,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.clazzName = it
                            }
                        })


                    umTextField(label = "${clazzDescLabel.text}",
                        value = entity?.clazzDesc,
                        error = clazzDescLabel.error,
                        disabled = !fieldsEnabled,
                        helperText = clazzDescLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.clazzDesc = it
                            }
                        })

                    umGridContainer(GridSpacing.spacing4) {

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umDatePicker(
                                label = "${startDateLabel.text}",
                                error = startDateLabel.error,
                                helperText = startDateLabel.errorText,
                                value = entity?.clazzStartTime.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzStartTime = it.getTime().toLong()
                                        clazzStartDateError = null
                                    }
                                })
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umDatePicker(
                                label = "${endDateLabel.text}",
                                error = endDateLabel.error,
                                helperText = endDateLabel.errorText,
                                value = entity?.clazzEndTime.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzEndTime = it.getTime().toLong()
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
                        renderSchedules(presenter.scheduleOneToManyJoinListener, scheduleList, createNewItem){
                            mPresenter?.scheduleOneToManyJoinListener?.onClickEdit(it)
                        }
                    }

                    umSpacer()

                    umTextField(label = "${schoolNameLabel.text}",
                        helperText = schoolNameLabel.errorText,
                        value = entity?.school?.schoolName,
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined){
                        attrs.asDynamic().onClick = {
                            mPresenter?.handleClickSchool()
                        }
                    }


                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${timeZoneLabel.text}",
                                value = entity?.clazzTimeZone,
                                error = timeZoneLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = timeZoneLabel.errorText,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzTimeZone = it
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleClickTimezone()
                                }
                            }
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${holidayCalenderLabel.text}",
                                value = entity?.holidayCalendar?.umCalendarName,
                                error = holidayCalenderLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = holidayCalenderLabel.errorText,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        clazzEndDateError = null
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleHolidayCalendarClicked()
                                }
                            }
                        }
                    }

                    umTextField(label = "${featureLabel.text}",
                        helperText = featureLabel.errorText,
                        value = setBitmaskListText(systemImpl,entity?.clazzFeatures),
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.clazzFeatures = it.toLong()
                            }
                        }){
                        attrs.asDynamic().onClick = {
                            mPresenter?.handleClickFeatures()
                        }
                    }

                    createListSectionTitle(getString(MessageID.permissions))

                    mPresenter?.let { presenter ->
                        scopeList?.let { scopeList ->

                            val newItem = CreateNewItem(true, MessageID.add_person_or_group){
                                mPresenter?.scopedGrantOneToManyHelper?.onClickNew()
                            }

                            renderScopedGrants(presenter.scopedGrantOneToManyHelper,
                                scopeList.distinctBy { it.name }, newItem){ scope ->
                                mPresenter?.scopedGrantOneToManyHelper?.onClickEdit(scope)
                            }
                        }
                    }

                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}
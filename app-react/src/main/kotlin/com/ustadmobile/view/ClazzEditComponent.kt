package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.view.ext.*
import kotlinx.css.marginTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzEditComponent (mProps: RProps): UstadEditComponent<ClazzWithHolidayCalendarAndSchool>(mProps),
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

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>?
        get() = TODO("Not yet implemented")
        set(value) {}

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

    override var scopedGrants: DoorLiveData<List<ScopedGrantAndName>>?
        get() = TODO("Not yet implemented")
        set(value) {}

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

    override fun onCreate() {
        super.onCreate()
        title = getString(MessageID.add_a_new_class).format(getString(MessageID.edit_clazz))
        mPresenter = ClazzEdit2Presenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
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
                        value = entity?.clazzName, error = clazzNameLabel.error,
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
                        error = clazzDescLabel.error, disabled = !fieldsEnabled,
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
                            mTextField(label = "${startDateLabel.text}",
                                value = Date(entity?.clazzStartTime ?: 0).standardFormat(),
                                error = startDateLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = startDateLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.clazzStartTime = it.targetInputValue.toLong()
                                        clazzStartDateError = null
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${endDateLabel.text}",
                                value = Date(entity?.clazzEndTime ?: 0).standardFormat(),
                                error = endDateLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = endDateLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.clazzEndTime = it.targetInputValue.toLong()
                                        clazzEndDateError = null
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }
                    }

                    createListSectionTitle(getString(MessageID.schedule))


                    mTextField(label = "${schoolNameLabel.text}",
                        helperText = schoolNameLabel.errorText,
                        value = entity?.school?.schoolName,
                        error = clazzNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                //entity?.clazzFeatures = it.targetInputValue.toLong()
                            }
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
                                        //entity?.holidayCalendar. = it.targetInputValue
                                        clazzEndDateError = null
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleClickHolidayCalendar()
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

                }

            }
        }
    }

}
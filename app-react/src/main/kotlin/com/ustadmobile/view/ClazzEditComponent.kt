package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MIconButtonSize
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formattedInHoursAndMinutes
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.view.ext.*
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
                            mTextField(label = "${startDateLabel.text}",
                                value = (if(entity?.clazzStartTime == null) Date() else Date(entity?.clazzStartTime!!)).standardFormat(),
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
                                value = (if(entity?.clazzEndTime == null) Date() else Date(entity?.clazzEndTime!!)).standardFormat(),
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

                    val createNewItem = CreateNewItem(true, MessageID.add_a_schedule){
                        mPresenter?.scheduleOneToManyJoinListener?.onClickNew()
                    }

                    mPresenter?.let { presenter ->
                        scheduleList?.let { schedules ->
                            renderSchedules(presenter, schedules, createNewItem){
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

                    mPresenter?.let { presenter ->
                        scopeList?.let { scopeList ->

                            val newItem = CreateNewItem(true, MessageID.add_person_or_group){
                                mPresenter?.scopedGrantOneToManyHelper?.onClickNew()
                            }

                            renderScopedGrants(presenter, scopeList, newItem){ scope ->
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

class ScopeGrantListComponent(mProps: ListProps<ScopedGrantAndName>): UstadSimpleList<ListProps<ScopedGrantAndName>>(mProps){

    override fun RBuilder.renderListItem(item: ScopedGrantAndName) {
        val showDeleteIcon = item.scopedGrant?.sgFlags?.hasFlag(ScopedGrant.FLAG_NO_DELETE) == true
        val permissionList = permissionListText(systemImpl,Clazz.TABLE_ID,
            item.scopedGrant?.sgPermissions ?: 0)
        val listener = (props.presenter as ClazzEdit2Presenter).scopedGrantOneToManyHelper
        if(showDeleteIcon){
            createItemWithIconTitleDescriptionAndIconBtn("admin_panel_settings",
                "delete",item.name, permissionList){
                listener.onClickDelete(item)
            }
        }else{
            createItemWithIconTitleAndDescription("admin_panel_settings",
                item.name, permissionList)
        }
    }

}

fun RBuilder.renderScopedGrants(presenter: ClazzEdit2Presenter,
                             scopes: List<ScopedGrantAndName>,
                             createNewItem: CreateNewItem = CreateNewItem(),
                             onEntryClicked: ((ScopedGrantAndName) -> Unit)? = null) = child(ScopeGrantListComponent::class) {
    attrs.entries = scopes
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.presenter = presenter
}

class ScheduleListComponent(mProps: ListProps<Schedule>): UstadSimpleList<ListProps<Schedule>>(mProps){

    override fun RBuilder.renderListItem(item: Schedule) {
        umGridContainer {
            val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
                .firstOrNull { it.optionVal == item.scheduleFrequency }?.messageId ?: MessageID.None
            val dayMessageId = ScheduleEditPresenter.DayOptions.values()
                .firstOrNull { it.optionVal == item.scheduleDay }?.messageId ?: MessageID.None

            val scheduleDays = "${systemImpl.getString(frequencyMessageId, this)} - ${systemImpl.getString(dayMessageId, this)}"

            val startEndTime = "${Date(item.sceduleStartTime).formattedInHoursAndMinutes()} " +
                    "- ${Date(item.scheduleEndTime).formattedInHoursAndMinutes()}"

            umItem(MGridSize.cells10, MGridSize.cells11){
                mTypography("$scheduleDays $startEndTime",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary,
                    gutterBottom = true){
                    css(alignTextToStart)
                }
            }

            umItem(MGridSize.cells2, MGridSize.cells1){
                mIconButton("delete", size = MIconButtonSize.small, onClick = {
                    val joinEditListener = (props.presenter as ClazzEdit2Presenter).scheduleOneToManyJoinListener
                    joinEditListener.onClickDelete(item)
                })
            }
        }
    }

}

fun RBuilder.renderSchedules(presenter: ClazzEdit2Presenter,
                             schedules: List<Schedule>,
                             createNewItem: CreateNewItem = CreateNewItem(),
                             onEntryClicked: ((Schedule) -> Unit)? = null) = child(ScheduleListComponent::class) {
    attrs.entries = schedules
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.presenter = presenter
}
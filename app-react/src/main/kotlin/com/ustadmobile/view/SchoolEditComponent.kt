package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.*
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class SchoolEditComponent (mProps: RProps): UstadEditComponent<SchoolWithHolidayCalendar>(mProps),
    SchoolEditView {

    private var mPresenter: SchoolEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    override val viewName: String
        get() = SchoolEditView.VIEW_NAME

    private var nameLabel = FieldLabel(text = getString(MessageID.class_name))

    private var descLabel = FieldLabel(text = getString(MessageID.class_description))

    private var timeZoneLabel = FieldLabel(text = getString(MessageID.timezone))

    private var holidayCalenderLabel = FieldLabel(text = getString(MessageID.holiday_calendar))

    private var phoneLabel = FieldLabel(text = getString(MessageID.phone))

    private var addressLabel = FieldLabel(text = getString(MessageID.address))

    private var emailLabel = FieldLabel(text = getString(MessageID.email))


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

    override var entity: SchoolWithHolidayCalendar? = null
        get() = field
        set(value) {
            if(value?.schoolName != null){
                title = value.schoolName
            }
            setState{
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.add_a_new_school, MessageID.edit_school)
        mPresenter = SchoolEditPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
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

                    mTextField(label = "${nameLabel.text}",
                        helperText = nameLabel.errorText,
                        value = entity?.schoolName,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.schoolName = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }


                    mTextField(label = "${descLabel.text}",
                        value = entity?.schoolDesc,
                        error = descLabel.error,
                        disabled = !fieldsEnabled,
                        helperText = descLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.schoolDesc = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${timeZoneLabel.text}",
                                value = entity?.schoolTimeZone,
                                error = timeZoneLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = timeZoneLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.schoolTimeZone = it.targetInputValue
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleTimeZoneClicked()
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
                                variant = MFormControlVariant.outlined){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleHolidayCalendarClicked()
                                }
                                css(defaultFullWidth)
                            }
                        }
                    }

                    mTextField(label = "${addressLabel.text}",
                        helperText = addressLabel.errorText,
                        value = entity?.schoolAddress,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.schoolAddress = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${emailLabel.text}",
                                helperText = emailLabel.errorText,
                                value = entity?.schoolEmailAddress,
                                error = emailLabel.error,
                                type = InputType.email,
                                disabled = !fieldsEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.schoolEmailAddress = it.targetInputValue
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${phoneLabel.text}",
                                helperText = phoneLabel.errorText,
                                value = entity?.schoolPhoneNumber,
                                error = phoneLabel.error,
                                disabled = !fieldsEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.schoolPhoneNumber = it.targetInputValue
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }
                    }

                    mSpacer()

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
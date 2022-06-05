package com.ustadmobile.view

import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.*
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css
import styled.styledDiv

class SchoolEditComponent (mProps: UmProps): UstadEditComponent<SchoolWithHolidayCalendar>(mProps),
    SchoolEditView {

    private var mPresenter: SchoolEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SchoolWithHolidayCalendar>?
        get() = mPresenter

    private var nameLabel = FieldLabel(text = getString(MessageID.name))

    private var descLabel = FieldLabel(text = getString(MessageID.description))

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
            field = value
            field?.removeObserver(scopedGrantListObserver)
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
                ustadComponentTitle = value.schoolName
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
            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells4){
                    umEntityAvatar(fallbackSrc = ASSET_ENTRY, listItem = true)
                }

                umItem(GridSize.cells12, GridSize.cells8){

                    renderListSectionTitle(getString(MessageID.basic_details))

                    umTextField(label = "${nameLabel.text}",
                        helperText = nameLabel.errorText,
                        value = entity?.schoolName,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.schoolName = it
                            }
                        })


                    umTextField(label = "${descLabel.text}",
                        value = entity?.schoolDesc,
                        error = descLabel.error,
                        disabled = !fieldsEnabled,
                        helperText = descLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            
                            setState {
                                entity?.schoolDesc = it
                            }
                        })

                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${timeZoneLabel.text}",
                                value = entity?.schoolTimeZone,
                                error = timeZoneLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = timeZoneLabel.errorText,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    
                                    setState {
                                        entity?.schoolTimeZone = it
                                    }
                                }){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleTimeZoneClicked()
                                }
                            }
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${holidayCalenderLabel.text}",
                                value = entity?.holidayCalendar?.umCalendarName,
                                error = holidayCalenderLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = holidayCalenderLabel.errorText,
                                variant = FormControlVariant.outlined){
                                attrs.asDynamic().onClick = {
                                    mPresenter?.handleHolidayCalendarClicked()
                                }
                            }
                        }
                    }

                    umTextField(label = "${addressLabel.text}",
                        helperText = addressLabel.errorText,
                        value = entity?.schoolAddress,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            
                            setState {
                                entity?.schoolAddress = it
                            }
                        })

                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${emailLabel.text}",
                                helperText = emailLabel.errorText,
                                value = entity?.schoolEmailAddress,
                                error = emailLabel.error,
                                type = InputType.email,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    
                                    setState {
                                        entity?.schoolEmailAddress = it
                                    }
                                })
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${phoneLabel.text}",
                                helperText = phoneLabel.errorText,
                                value = entity?.schoolPhoneNumber,
                                error = phoneLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.schoolPhoneNumber = it
                                    }
                                })
                        }
                    }

                    umSpacer()

                    renderListSectionTitle(getString(MessageID.permissions))

                    mPresenter?.let { presenter ->
                        scopeList?.let { scopeList ->
                            val newItem = CreateNewItem(true, getString(MessageID.add_person_or_group)){
                                mPresenter?.scopedGrantOneToManyHelper?.onClickNew()
                            }

                            renderScopedGrants(presenter.scopedGrantOneToManyHelper, scopeList.distinctBy { it.name}, newItem){ scope ->
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
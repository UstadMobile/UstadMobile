package com.ustadmobile.view

import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umListItem
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.view.ext.createListItemWithLeftIconTitleAndDescription
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv


class SettingsComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), SettingsView {

    override val viewNames: List<String>
        get() = listOf(SettingsView.VIEW_NAME)

    var mPresenter: SettingsPresenter? = null

    override var workspaceSettingsVisible: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var holidayCalendarVisible: Boolean = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var reasonLeavingVisible: Boolean = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var langListVisible: Boolean = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.settings)
        mPresenter = SettingsPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +StyleManager.contentContainer
                +StyleManager.defaultPaddingTop
            }
            umGridContainer(rowSpacing = GridSpacing.spacing1) {

                if(holidayCalendarVisible){
                    umItem(GridSize.cells12){
                        umListItem(button = true) {
                            attrs.onClick = {
                                stopEventPropagation(it)
                                mPresenter?.goToHolidayCalendarList()
                            }
                            createListItemWithLeftIconTitleAndDescription("date_range",
                                getString(MessageID.holiday_calendars),
                                getString(MessageID.holiday_calendars_desc),
                                onMainList = true
                            )
                        }
                    }

                }

                if(reasonLeavingVisible){
                    umItem(GridSize.cells12){
                        umListItem(button = true) {
                            attrs.onClick = {
                                stopEventPropagation(it)
                                mPresenter?.handleClickLeavingReason()
                            }
                            createListItemWithLeftIconTitleAndDescription("logout",
                                getString(MessageID.leaving_reason),
                                getString(MessageID.leaving_reason_manage),
                                onMainList = true
                            )
                        }
                    }
                }

                if(langListVisible){
                    umItem(GridSize.cells12){
                        umListItem(button = true) {
                            attrs.onClick = {
                                stopEventPropagation(it)
                                mPresenter?.handleClickLangList()
                            }
                            createListItemWithLeftIconTitleAndDescription("language",
                                getString(MessageID.languages),
                                getString(MessageID.languages_description),
                                onMainList = true
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }

}
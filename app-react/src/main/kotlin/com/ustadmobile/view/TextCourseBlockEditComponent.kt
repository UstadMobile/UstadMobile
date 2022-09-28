package com.ustadmobile.view

import com.ustadmobile.core.controller.TextCourseBlockEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.TextCourseBlockEditView
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class TextCourseBlockEditComponent (mProps: UmProps): UstadEditComponent<CourseBlock>(mProps),
    TextCourseBlockEditView {

    private var mPresenter: TextCourseBlockEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlock>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    override var blockTitleError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                titleLabel = titleLabel.copy(errorText = field)
            }
        }

    override var startDate: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var startTime: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: CourseBlock? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = TextCourseBlockEditPresenter(this, arguments, this,di,this)
        setEditTitle(MessageID.add_text, MessageID.edit_text)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +fieldsOnlyFormScreen
            }

            umItem(GridSize.cells12){
                umTextField(label = "${titleLabel.text}",
                    helperText = titleLabel.errorText,
                    value = entity?.cbTitle, error = titleLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.cbTitle = it
                            blockTitleError = null
                        }
                    })
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells6){
                    umDatePicker(
                        label = "${doNotShowBeforeLabel.text}",
                        error = doNotShowBeforeLabel.error,
                        helperText = doNotShowBeforeLabel.errorText,
                        value = startDate.toDate(true),
                        inputVariant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                startDate = it.getTime().toLong()
                            }
                        }
                    )
                }

                umItem(GridSize.cells12, GridSize.cells6){
                    umTimePicker(
                        label = "${startTimeLabel.text}",
                        error = startTimeLabel.error,
                        helperText = startTimeLabel.errorText,
                        value = startDate.toDate(true),
                        inputVariant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                startDate = it.getTime().toLong()
                            }
                        }
                    )
                }
            }

            umItem{
                umSpacer()
                umMuiHtmlEditor(
                    entity?.cbDescription,
                    label = getString(MessageID.type_here),
                    onChange = {
                    entity?.cbDescription = it
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        blockTitleError = null
    }

}
package com.ustadmobile.view

import com.ustadmobile.core.controller.ModuleCourseBlockEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ModuleCourseBlockEditView
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.*
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import io.github.aakira.napier.Napier
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ModuleCourseBlockEditComponent (mProps: UmProps): UstadEditComponent<CourseBlock>(mProps),
    ModuleCourseBlockEditView {

    private var mPresenter: ModuleCourseBlockEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlock>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))

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
        mPresenter = ModuleCourseBlockEditPresenter(this, arguments, this,di,this)
        setEditTitle(MessageID.add_module, MessageID.edit_module)
        Napier.d("ModuleCourseBlockEditComponent: navController viewName = ${navController.currentBackStackEntry?.viewName}" +
            "stateHandle=${(navController.currentBackStackEntry?.savedStateHandle as? UstadSavedStateHandleJs)?.dumpToString()}")
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

            umItem(GridSize.cells12){
                umTextField(label = "${descriptionLabel.text}",
                    helperText = descriptionLabel.errorText,
                    value = entity?.cbDescription,
                    error = descriptionLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.cbDescription = it
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
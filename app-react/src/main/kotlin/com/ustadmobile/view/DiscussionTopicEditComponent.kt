package com.ustadmobile.view

import com.ustadmobile.core.controller.DiscussionTopicEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umItem
import io.github.aakira.napier.Napier
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class DiscussionTopicEditComponent (mProps: UmProps): UstadEditComponent<DiscussionTopic>(mProps),
    DiscussionTopicEditView {

    private var mPresenter: DiscussionTopicEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DiscussionTopic>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.add_topic))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))


    override var blockTitleError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                titleLabel = titleLabel.copy(errorText = field)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: DiscussionTopic? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = DiscussionTopicEditPresenter(this, arguments, this,this, di)
        setEditTitle(MessageID.add_module, MessageID.edit_module)
        Napier.d("DiscussionTopicEditComponent: navController viewName = ${navController.currentBackStackEntry?.viewName}" +
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
                    value = entity?.discussionTopicTitle, error = titleLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.discussionTopicTitle = it
                            blockTitleError = null
                        }
                    })
            }

            umItem(GridSize.cells12){
                umTextField(label = "${descriptionLabel.text}",
                    helperText = descriptionLabel.errorText,
                    value = entity?.discussionTopicDesc,
                    error = descriptionLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.discussionTopicDesc = it
                            blockTitleError = null
                        }
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
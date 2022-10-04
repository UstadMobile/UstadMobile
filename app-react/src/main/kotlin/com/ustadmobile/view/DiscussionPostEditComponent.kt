package com.ustadmobile.view

import com.ustadmobile.core.controller.DiscussionPostEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umItem
import io.github.aakira.napier.Napier
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class DiscussionPostEditComponent (mProps: UmProps): UstadEditComponent<DiscussionPost>(mProps),
    DiscussionPostEditView {

    private var mPresenter: DiscussionPostEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DiscussionPost>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.message))


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

    override var entity: DiscussionPost? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = DiscussionPostEditPresenter(this, arguments, this,di, this)
        setEditTitle(MessageID.post, MessageID.post)
        Napier.d("DiscussionPostEditComponent: navController viewName = ${navController.currentBackStackEntry?.viewName}" +
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
                    value = entity?.discussionPostTitle, error = titleLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.discussionPostTitle = it
                            blockTitleError = null
                        }
                    })
            }

            umItem(GridSize.cells12){
                umTextField(label = "${descriptionLabel.text}",
                    helperText = descriptionLabel.errorText,
                    value = entity?.discussionPostMessage,
                    error = descriptionLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.discussionPostMessage = it
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
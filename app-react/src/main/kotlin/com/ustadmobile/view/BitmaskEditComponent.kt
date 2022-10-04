package com.ustadmobile.view

import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.core.view.BitmaskEditView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class BitmaskEditComponent (mProps: UmProps): UstadEditComponent<LongWrapper>(mProps),
    BitmaskEditView {

    private var mPresenter: BitmaskEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, LongWrapper>?
        get() = mPresenter


    private var scopeList: List<BitmaskFlag>? = null

    private val scopedGrantListObserver = ObserverFnWrapper<List<BitmaskFlag>> {
        setState {
            scopeList = it
        }
    }

    override var bitmaskList: LiveData<List<BitmaskFlag>>? = null
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

    override var entity: LongWrapper? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.features_enabled, MessageID.features_enabled)
        mPresenter = BitmaskEditPresenter(this, arguments, this,
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

                scopeList?.let { scopes ->
                    child(ScopedGrantEditComponent.ScopedGrantComponent::class) {
                        attrs.entries = scopes
                        attrs.mainList = true
                        attrs.onEntryClicked = { it as BitmaskFlag
                           val scope = scopeList?.first { scope -> scope.messageId == it.messageId }
                            scope?.enabled = !(scope?.enabled ?: false)
                            setState {  }
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
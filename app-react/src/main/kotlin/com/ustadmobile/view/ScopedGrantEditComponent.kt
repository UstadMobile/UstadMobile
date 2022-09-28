package com.ustadmobile.view

import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListItemWithTitleAndSwitch
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ScopedGrantEditComponent (mProps: UmProps): UstadEditComponent<ScopedGrant>(mProps),
    ScopedGrantEditView{

    private var mPresenter: ScopedGrantEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ScopedGrant>?
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

    override var entity: ScopedGrant? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.people)
        mPresenter = ScopedGrantEditPresenter(this, arguments, this,
            this, di)
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
                    child(ScopedGrantComponent::class) {
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

    class ScopedGrantComponent(mProps: SimpleListProps<BitmaskFlag>): UstadSimpleList<SimpleListProps<BitmaskFlag>>(mProps){

        override fun RBuilder.renderListItem(item: dynamic, onClick: (Event) -> Unit) {

            renderListItemWithTitleAndSwitch(getString(item.messageId as Int), item.enabled as Boolean, onClick)
        }

    }

}
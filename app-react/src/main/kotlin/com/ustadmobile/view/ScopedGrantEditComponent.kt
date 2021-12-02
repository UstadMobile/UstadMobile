package com.ustadmobile.view

import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.switchMargin
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.LinearDimension
import kotlinx.css.marginBottom
import kotlinx.css.marginLeft
import kotlinx.css.marginTop
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ScopedGrantEditComponent (mProps: UmProps): UstadEditComponent<ScopedGrant>(mProps),
    ScopedGrantEditView{

    private var mPresenter: ScopedGrantEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ScopedGrant>?
        get() = mPresenter

    override val viewName: String
        get() = ScopedGrantEditView.VIEW_NAME


    private var scopeList: List<BitmaskFlag>? = null

    private val scopedGrantListObserver = ObserverFnWrapper<List<BitmaskFlag>> {
        setState {
            scopeList = it
        }
    }

    override var bitmaskList: DoorLiveData<List<BitmaskFlag>>? = null
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
        title = getString(MessageID.people)
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

    class ScopedGrantComponent(mProps: ListProps<BitmaskFlag>): UstadSimpleList<ListProps<BitmaskFlag>>(mProps){

        override fun RBuilder.renderListItem(item: BitmaskFlag) {
            umGridContainer {
                umItem(GridSize.cells10, GridSize.cells11){
                    umTypography(getString(item.messageId),
                        variant = TypographyVariant.body1,
                        gutterBottom = true){
                        css{
                            +StyleManager.alignTextToStart
                            marginTop = LinearDimension("3px")
                        }
                    }
                }

                umItem(GridSize.cells2, GridSize.cells1){
                    css{
                        +switchMargin
                    }
                    umSwitch(item.enabled, color = UMColor.secondary)
                }

                css{
                    marginLeft = LinearDimension("20px")
                    marginTop = LinearDimension("16px")
                    marginBottom = LinearDimension("16px")
                }
            }
        }

    }

}
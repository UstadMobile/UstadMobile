package com.ustadmobile.view

import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.MGridSpacing
import com.ustadmobile.core.controller.SchoolDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ACTIVE_TAB_INDEX
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.urlSearchParamsToMap
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class SchoolDetailComponent(mProps:RProps): UstadDetailComponent<School>(mProps), SchoolDetailView {

    private var mPresenter: SchoolDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = SchoolDetailView.VIEW_NAME

    override var entity: School? = null
        set(value) {
            field = value
            title = value?.schoolName
        }

    private var tabsToRender: List<UstadTab>? = null

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = SchoolDetailPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        val entityUidValue : String = arguments[UstadView.ARG_ENTITY_UID] ?:"0"
        val commonArgs = mapOf(UstadView.ARG_NAV_CHILD to true.toString())

        val tabs = listOf(
            SchoolDetailOverviewView.VIEW_NAME.appendQueryArgs(
                commonArgs + mapOf(UstadView.ARG_ENTITY_UID to entityUidValue)
            ),

            SchoolMemberListView.VIEW_NAME.appendQueryArgs(
                commonArgs + mapOf(
                    UstadView.ARG_FILTER_BY_ROLE to Role.ROLE_SCHOOL_STAFF_UID.toString(),
                    UstadView.ARG_FILTER_BY_SCHOOLUID to entityUidValue)
            ),
            SchoolMemberListView.VIEW_NAME.appendQueryArgs(
                commonArgs + mapOf(
                    UstadView.ARG_FILTER_BY_ROLE to Role.ROLE_SCHOOL_STUDENT_UID.toString(),
                    UstadView.ARG_FILTER_BY_SCHOOLUID to entityUidValue)
            )
        )

        setState {
            tabsToRender = tabs.mapIndexed { index, it ->
                val titles = listOf(MessageID.overview, MessageID.staff, MessageID.students)
                UstadTab(index,it.substringBefore("?"),
                    urlSearchParamsToMap(it.substring(it.lastIndexOf("?"))),
                    getString(titles[tabs.indexOf(it)]))
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +defaultPaddingTop
                +contentContainer
            }
            umGridContainer(MGridSpacing.spacing6) {
                umItem(MGridSize.cells12, MGridSize.cells4){
                    umEntityAvatar(listItem = true, fallbackSrc = Util.ASSET_ENTRY, iconName = "people",
                        showIcon = true)
                }

                umItem(MGridSize.cells12, MGridSize.cells8){
                    styledDiv {
                        css {
                            +StyleManager.clazzDetailExtraInfo
                        }

                        tabsToRender?.let { tabs ->
                            renderTabs(tabs,activeTabIndex = arguments[ARG_ACTIVE_TAB_INDEX]?.toInt() ?: 0)
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
        tabsToRender = null
    }
}
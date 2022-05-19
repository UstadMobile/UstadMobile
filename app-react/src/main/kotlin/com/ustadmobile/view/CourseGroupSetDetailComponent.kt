package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseGroupSetDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.umList
import com.ustadmobile.mui.components.umListItem
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umSpacer
import react.RBuilder
import react.setState
import styled.css

class CourseGroupSetDetailComponent(props: UmProps): UstadDetailComponent<CourseGroupSet>(props),
    CourseGroupSetDetailView {

    private var mPresenter: CourseGroupSetDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var memberList: List<CourseGroupMemberPerson>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var entity: CourseGroupSet? = null
        get() = field
        set(value) {
            field = value
            updateUiWithStateChangeDelay {
                ustadComponentTitle = value?.cgsName
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = CourseGroupSetDetailPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css(contentContainer)
            umList {
                css(horizontalList)
                memberList?.forEach {
                    if(it.personUid == 0L){
                        umSpacer()
                        renderListSectionTitle(getString(MessageID.group_number).format(
                            it.member?.cgmGroupNumber.toString()))
                    }else {
                        umListItem {
                            renderListItemWithLeftIconTitleAndDescription(
                                "person",
                                it.fullName(),
                                titleVariant = TypographyVariant.h6,
                                onMainList = true)
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
package com.ustadmobile.view

import com.ustadmobile.core.controller.PeerReviewerAllocationEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UidOption
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocationList
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderListItemWithTitleAndOptionOnRight
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import kotlinx.css.height
import kotlinx.css.px
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class PeerReviewerAllocationEditComponent(mProps: UmProps): UstadEditComponent<PeerReviewerAllocationList>(mProps),
    PeerReviewerAllocationEditView{

    private var mPresenter: PeerReviewerAllocationEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PeerReviewerAllocationList>?
        get() = mPresenter

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: PeerReviewerAllocationList? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var submitterListWithAllocations: List<AssignmentSubmitterWithAllocations>? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.assign_reviewers, MessageID.assign_reviewers)
        mPresenter = PeerReviewerAllocationEditPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }


    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.contentContainer
                +StyleManager.defaultPaddingTop
            }


//            umItem(GridSize.cells12, GridSize.cells12) {
//                umButton(getString(MessageID.assign_random_reviewers),
//                    size = ButtonSize.large,
//                    color = UMColor.secondary,
//                    variant = ButtonVariant.contained,
//                    onClick = {
//                        mPresenter?.handleRandomAssign()
//                    }){
//                    css {
//                        +StyleManager.defaultFullWidth
//                        +StyleManager.defaultMarginTop
//                        height = 50.px
//                    }}
//            }

            umSpacer()

            umItem {
                umList {
                    css(StyleManager.horizontalList)
                    submitterListWithAllocations?.forEachIndexed { index, submitter ->

//                        renderListSectionTitle(
//                            submitter.name ?: "", TypographyVariant.h6)


                        val submitterList: MutableList<AssignmentSubmitterSummary> = submitterListWithAllocations?.toMutableList() ?: mutableListOf()
                        val self = submitterList.find { submit -> submit.submitterUid == submitter.submitterUid }
                        if(self != null){
                            submitterList.remove(self)
                        }
                        val submitterUidOption = mutableListOf<UidOption>()
                        submitterList.forEach {
                            submitterUidOption.add(UidOption(it.name ?: "", it.submitterUid))
                        }

                        submitter.allocations?.forEachIndexed { allocationIndex, peer ->

                            umListItem{
                                renderListItemWithTitleAndOptionOnRight(
                                    peer.praMarkerSubmitterUid.toString(),
                                    systemImpl.getString(MessageID.reviewer, this).format(allocationIndex + 1),
                                    options = submitterUidOption,
                                    fieldLabel = FieldLabel(text = systemImpl.getString(MessageID.reviewer, this).format(allocationIndex + 1)),
                                    onChange = {
                                        peer.praMarkerSubmitterUid = it.toLong()
                                        setState {  }
                                    }
                                )
                            }
                        }
                    }
                }
            }



        }

    }

}
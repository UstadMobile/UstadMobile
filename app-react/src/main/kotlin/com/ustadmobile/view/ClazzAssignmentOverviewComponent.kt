package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailOverviewPresenter
import com.ustadmobile.core.controller.DefaultContentEntryListItemListener
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.formattedInHoursAndMinutes
import com.ustadmobile.util.ext.fullDateFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.js.Date

class ClazzAssignmentOverviewComponent(mProps: UmProps): UstadDetailComponent<ClazzAssignment>(mProps),
    ClazzAssignmentDetailOverviewView {

    private var mPresenter: ClazzAssignmentDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var showScoreMetrics = false

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentDetailOverviewView.VIEW_NAME)

    private var showClassCommentDialog = false

    private var showPrivateCommentDialog = false

    private var classComments: List<CommentsWithPerson> = listOf()

    private var privateComments: List<CommentsWithPerson> = listOf()

    private var contents : List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = listOf()

    private val classCommentsObserver = ObserverFnWrapper<List<CommentsWithPerson>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            classComments = it
        }
    }

    private val privateCommentsObserver = ObserverFnWrapper<List<CommentsWithPerson>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            privateComments = it
        }
    }


    private val contentsObserver = ObserverFnWrapper<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            contents = it
        }
    }

    override var clazzMetrics: ContentEntryStatementScoreProgress? = null
        set(value) {
            setState {
                field = value
                showScoreMetrics = value?.resultMax ?: 0 > 0
            }
        }

    override var clazzAssignmentContent: DoorDataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(contentsObserver)
            liveData?.observe(this, contentsObserver)
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var clazzAssignmentClazzComments: DoorDataSourceFactory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(classCommentsObserver)
            liveData?.observe(this, classCommentsObserver)
        }

    override var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(privateCommentsObserver)
            liveData?.observe(this, privateCommentsObserver)
        }

    override var showPrivateComments: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ClazzAssignmentDetailOverviewPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(entity == null) return
        styledDiv {
            css {
                +defaultPaddingTop
                +contentContainer
            }

            umGridContainer(rowSpacing = GridSpacing.spacing3) {
                if(!entity?.caDescription.isNullOrBlank()){
                    umItem(GridSize.cells12){
                        umTypography(entity?.caDescription)
                    }
                }

                umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                    styledSpan {
                        css{
                            padding(right = 2.spacingUnits)
                        }
                        umIcon("event_available")
                    }

                    umTypography("${entity?.caDeadlineDate.toDate()?.fullDateFormat()} " +
                            "- ${entity?.caDeadlineDate.toDate()?.formattedInHoursAndMinutes()}"){
                        css{
                            marginTop = LinearDimension("2px")
                            +StyleManager.alignTextToStart
                            padding(right = 4.spacingUnits)
                        }
                    }

                    umTypography(timeZone){
                        css{
                            +StyleManager.alignTextToStart
                            padding(right = 4.spacingUnits)
                        }
                    }

                    if(showScoreMetrics){
                        umItem(GridSize.cells12){
                            umTypography("${clazzMetrics?.calculateScoreWithPenalty()}%")
                            umTypography(getString(MessageID.total_score))
                        }
                    }

                }

                if(contents.isNotEmpty()){
                    umItem(GridSize.cells12){
                        createListSectionTitle(getString(MessageID.content), TypographyVariant.h6)

                        val entryItemListener = DefaultContentEntryListItemListener(context = this, di = di,
                            clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L)
                        umList {
                            css(horizontalList)
                            for (content in contents) {
                                umListItem(button = true) {
                                    css{
                                        width = LinearDimension("97%")
                                    }
                                   createContentEntryListItem(content, systemImpl, false,
                                       mainList = false, onClick = {
                                       entryItemListener.onClickContentEntry(it)
                                   }){}
                                }
                            }
                        }
                        umSpacer()
                    }
                }

                if(entity?.caClassCommentEnabled == true){
                    umGridContainer(rowSpacing = GridSpacing.spacing2) {
                        umItem(GridSize.cells12){
                            createListSectionTitle(getString(MessageID.class_comments), TypographyVariant.h6)
                        }

                        val label = getString(MessageID.add_class_comment)
                        renderCreateCommentSection(label){
                            setState {
                                showClassCommentDialog = true
                            }
                        }

                        if(showClassCommentDialog){
                            renderCreateNewComment(label,
                                listener = mPresenter?.newClassCommentListener,
                                systemImpl = systemImpl,
                                shownAt = Date().getTime().toLong()){
                                setState {
                                    showClassCommentDialog = false
                                }
                            }
                        }

                        umItem(GridSize.cells12){
                            renderComments(classComments)
                        }
                    }
                }

                if(showPrivateComments){
                    umGridContainer(rowSpacing = GridSpacing.spacing2) {
                        umItem(GridSize.cells12){
                            createListSectionTitle(getString(MessageID.private_comments), TypographyVariant.h6)
                        }

                        val label = getString(MessageID.add_private_comment)
                        renderCreateCommentSection(label){
                            setState {
                                showPrivateCommentDialog = true
                            }
                        }

                        if(showPrivateCommentDialog){
                            renderCreateNewComment(label,
                                listener = mPresenter?.newPrivateCommentListener,
                                systemImpl = systemImpl,
                                shownAt = Date().getTime().toLong()){
                                setState {
                                    showPrivateCommentDialog = false
                                }
                            }
                        }

                        umItem(GridSize.cells12){
                            renderComments(privateComments)
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
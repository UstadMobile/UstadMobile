package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzAssignmentDetailStudentProgressComponent(mProps: UmProps): UstadDetailComponent<ClazzAssignment>(mProps),
    ClazzAssignmentDetailStudentProgressView {

    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var showPrivateCommentDialog = false

    override val viewName: String
        get() = ClazzAssignmentDetailStudentProgressView.VIEW_NAME

    private var privateComments: List<CommentsWithPerson> = listOf()

    private var contents : List<ContentWithAttemptSummary> = listOf()

    private val privateCommentsObserver = ObserverFnWrapper<List<CommentsWithPerson>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            privateComments = it
        }
    }

    private val contentsObserver = ObserverFnWrapper<List<ContentWithAttemptSummary>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            contents = it
        }
    }

    override var person: Person? = null
        get() = field
        set(value) {
            setState {
                field = value
                ustadComponentTitle = value?.fullName()
            }
        }

    override var clazzAssignmentContent: DoorDataSourceFactory<Int, ContentWithAttemptSummary>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(contentsObserver)
            liveData?.observe(this, contentsObserver)
        }

    override var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(privateCommentsObserver)
            liveData?.observe(this, privateCommentsObserver)
        }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            setState {
                field = value
                console.log(value)
            }
        }

    override var studentScore: ContentEntryStatementScoreProgress? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ClazzAssignmentDetailStudentProgressPresenter(this,
            arguments, this,  di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(entity == null) return
        styledDiv {
            css {
                +listComponentContainer
                +contentContainer
            }

            umGridContainer(rowSpacing = GridSpacing.spacing3) {

                if(contents.isNotEmpty()){
                    umItem(GridSize.cells12){
                        createListSectionTitle(getString(MessageID.content), TypographyVariant.h6)
                        umList {
                            css(horizontalList)
                            for (content in contents) {
                                umListItem(button = true) {
                                    css{
                                        width = LinearDimension("100%")
                                    }
                                   createContentEntryListItemWithAttemptsAndProgress(systemImpl, content){
                                       mPresenter?.onClickContentWithAttempt(it)
                                   }
                                }
                            }
                        }
                        umSpacer()
                    }
                }

                if(studentScore != null){
                    umItem(GridSize.cells12, GridSize.cells3) {
                       umGridContainer {
                           createSummaryCard("${studentScore?.calculateScoreWithPenalty()}%  " +
                                   "(${studentScore?.resultScore}/${studentScore?.resultMax})",
                               getString(MessageID.total_score))
                       }
                    }
                }

                if(entity?.caPrivateCommentsEnabled == true){
                    umItem(GridSize.cells12){
                        umGridContainer(rowSpacing = GridSpacing.spacing1) {

                            umItem(GridSize.cells12){
                                createListSectionTitle(getString(MessageID.private_comments), TypographyVariant.h6)
                            }

                            umItem(GridSize.cells12) {
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
                            }

                            umItem(GridSize.cells12){
                                renderComments(privateComments)
                            }
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
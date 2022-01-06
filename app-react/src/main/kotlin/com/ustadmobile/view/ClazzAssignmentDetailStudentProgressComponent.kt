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
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatToStringHoursMinutesSeconds
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.js.Date

class ClazzAssignmentDetailStudentProgressComponent(mProps: UmProps): UstadDetailComponent<ClazzAssignment>(mProps),
    ClazzAssignmentDetailStudentProgressView {

    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var showPrivateCommentDialog = false

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentDetailStudentProgressView.VIEW_NAME)

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

                                    umGridContainer {
                                        attrs.onClick = {
                                            Util.stopEventPropagation(it)
                                            mPresenter?.onClickContentWithAttempt(content)
                                        }
                                        umItem(GridSize.cells4, GridSize.cells2){
                                            umGridContainer {
                                                umItem(GridSize.cells12) {
                                                    umItemThumbnail("class",content.contentEntryThumbnailUrl, width = 80,
                                                        iconColor = Color(StyleManager.theme.palette.action.disabled),
                                                        avatarBackgroundColor = Color.transparent)
                                                }

                                                if(content.scoreProgress?.progress ?: 0 > 0){
                                                    umItem(GridSize.cells12) {
                                                        umLinearProgress(content.scoreProgress?.progress?.toDouble(),
                                                            variant = ProgressVariant.determinate){
                                                            css{
                                                                marginTop = 1.spacingUnits
                                                                width = LinearDimension("80px")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        umItem(GridSize.cells8, GridSize.cells10){
                                            umGridContainer {

                                                umItem(GridSize.cells12){
                                                    umTypography(content.contentEntryTitle,
                                                        variant = TypographyVariant.h6){
                                                        css(StyleManager.alignTextToStart)
                                                    }
                                                }

                                                umItem(GridSize.cells12, GridSize.cells3, flexDirection = FlexDirection.row){
                                                    styledSpan {
                                                        css{
                                                            padding(right = 3.spacingUnits)
                                                        }
                                                        umIcon("restore", fontSize = IconFontSize.small){
                                                            css{
                                                                marginTop = 1.px
                                                            }
                                                        }
                                                    }

                                                    umTypography("${content.attempts} ${systemImpl.getString(MessageID.attempts, this)}",
                                                        variant = TypographyVariant.body2){
                                                        css(StyleManager.alignTextToStart)
                                                    }
                                                }

                                                if(content.startDate > 0){
                                                    umItem(GridSize.cells12, GridSize.cells3) {
                                                        val endDate = if(content.endDate == 0L) "" else " - ${content.endDate.toDate()?.standardFormat()}"
                                                        umTypography("${content.startDate.toDate()?.standardFormat()}$endDate",
                                                            variant = TypographyVariant.body2){
                                                            css (StyleManager.alignTextToStart)
                                                        }
                                                    }
                                                }

                                                if(content.duration > 60000){
                                                    umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                                                        styledSpan {
                                                            css{
                                                                padding(right = 2.spacingUnits)
                                                            }
                                                            umIcon("timer", fontSize = IconFontSize.small){
                                                                css{
                                                                    marginTop = 1.px
                                                                }
                                                            }
                                                        }

                                                        styledSpan {
                                                            css{
                                                                padding(right = 2.spacingUnits)
                                                            }

                                                            umTypography(content.duration.formatToStringHoursMinutesSeconds(systemImpl),
                                                                variant = TypographyVariant.body1,
                                                                paragraph = true){
                                                                css(StyleManager.alignTextToStart)
                                                            }
                                                        }
                                                    }
                                                }

                                                umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                                                    styledSpan {
                                                        css{
                                                            padding(right = 3.spacingUnits)
                                                        }
                                                        umIcon("emoji_events", fontSize = IconFontSize.small){
                                                            css{
                                                                marginTop = 1.px
                                                            }
                                                        }
                                                    }

                                                    umTypography("${content.scoreProgress?.calculateScoreWithPenalty()}%",
                                                        variant = TypographyVariant.body2){
                                                        css(StyleManager.alignTextToStart)
                                                    }

                                                    val hideScore = content.scoreProgress == null || (content.scoreProgress?.resultScore == 0 && content.scoreProgress?.progress == 0)

                                                    if(!hideScore){
                                                        styledSpan {
                                                            css{
                                                                padding(left = 3.spacingUnits)
                                                            }
                                                            umTypography("(${content.scoreProgress?.resultScore} / ${content.scoreProgress?.resultMax})",
                                                                variant = TypographyVariant.body2){
                                                                css(StyleManager.alignTextToStart)
                                                            }
                                                        }
                                                    }


                                                    if(content.scoreProgress?.penalty ?:0 > 0){
                                                        styledSpan {
                                                            css{
                                                                padding(left = 3.spacingUnits)
                                                            }
                                                            umTypography(systemImpl.getString(MessageID.late_penalty, this).format(content.scoreProgress?.penalty ?: 0),
                                                                variant = TypographyVariant.body2){
                                                                css(StyleManager.alignTextToStart)
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        umSpacer()
                    }
                }

                if(studentScore != null){
                    umItem(GridSize.cells12) {
                        createSummaryCard("${studentScore?.calculateScoreWithPenalty()}%  " +
                                "(${studentScore?.resultScore}/${studentScore?.resultMax})",
                            getString(MessageID.total_score))
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
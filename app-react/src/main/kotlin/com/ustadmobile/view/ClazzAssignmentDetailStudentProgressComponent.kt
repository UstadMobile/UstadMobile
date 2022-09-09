package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressPresenter
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.DraftJsUtil.clean
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatFullDate
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import kotlinx.css.height
import kotlinx.css.px
import mui.material.ButtonVariant
import mui.material.FormControlVariant
import mui.material.InputLabelVariant
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css
import styled.styledDiv

class ClazzAssignmentDetailStudentProgressComponent(mProps: UmProps): UstadDetailComponent<ClazzAssignmentWithCourseBlock>(mProps),
    ClazzAssignmentDetailStudentProgressView {

    private var mPresenter: ClazzAssignmentDetailStudentProgressPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var markGrade = ""

    private var markLabel = FieldLabel(text = getString(MessageID.points ))

    private var privateComments: List<CommentsWithPerson> = listOf()

    private var contents : List<ContentWithAttemptSummary> = listOf()

    private var submissions : List<CourseAssignmentSubmissionWithAttachment> = listOf()

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

    private val submissionsObserver = ObserverFnWrapper<List<CourseAssignmentSubmissionWithAttachment>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            submissions = it
        }
    }
    override var submitMarkError: String? = null
        get() = field
        set(value) {
            setState {
                markLabel = markLabel.copy(errorText =  value)
            }
        }

    override var submitterName: String? = null
        get() = field
        set(value) {
            setState {
                field = value
                ustadComponentTitle = value
            }
        }
    override var clazzCourseAssignmentSubmissionAttachment: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(submissionsObserver)
            liveData?.observe(this, submissionsObserver)
        }

    override var clazzAssignmentPrivateComments: DataSourceFactory<Int, CommentsWithPerson>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(privateCommentsObserver)
            liveData?.observe(this, privateCommentsObserver)
        }

    override var submissionScore: CourseAssignmentMark? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var submissionStatus: Int = 0
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var markNextStudentVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var submitButtonVisible: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzAssignmentWithCourseBlock? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.GONE
        mPresenter = ClazzAssignmentDetailStudentProgressPresenter(this,
            arguments, this,  di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +listComponentContainer
                +contentContainer
            }

            umGridContainer(rowSpacing = GridSpacing.spacing3) {

                umItem {
                    renderListSectionTitle(getString(MessageID.submissions), TypographyVariant.h6)
                }

                umItem {

                    renderInformationOnDetailScreen(
                        if(submissionStatus == CourseAssignmentSubmission.NOT_SUBMITTED) null
                        else ClazzAssignmentDetailOverviewComponent.ASSIGNMENT_STATUS_MAP[submissionStatus],
                        getString(SubmissionConstants.STATUS_MAP[submissionStatus] ?: 0),
                        getString(MessageID.status),
                        shrink = true
                    )

                    val mark = submissionScore
                    if(mark != null){

                        val marks = "${mark.camMark} / ${entity?.block?.cbMaxPoints} ${getString(MessageID.points)}"

                        val penalty = if(mark.camPenalty != 0)
                            " ${getString(MessageID.late_penalty).format(entity?.block?.cbLateSubmissionPenalty ?: "")}"
                        else
                            ""

                        renderInformationOnDetailScreen("emoji_events",
                            "$marks$penalty",
                            getString(MessageID.xapi_result_header),
                            shrink = true
                        )
                    }
                }

                if(submissions.isNotEmpty()){
                    umItem {
                        umList {
                            submissions.forEach { submission ->
                                umListItem {
                                    attrs.onClick = {
                                        Util.stopEventPropagation(it)
                                        mPresenter?.onClickOpenSubmission(submission/*, false*/)
                                    }
                                    renderListItemWithLeftIconTitleAndDescription(
                                        "class",
                                        clean(submission.attachment?.casaFileName ?: submission.casText),
                                        "${getString(MessageID.submitted_cap)} " +
                                                ": ${submission.casTimestamp.toDate()?.formatFullDate()}",
                                        true,
                                    )
                                }
                            }
                        }
                    }
                }

                umGridContainer(GridSpacing.spacing4) {
                    css(defaultDoubleMarginTop)

                    if(submitButtonVisible){

                        umItem(GridSize.cells12, GridSize.cells4) {
                            umFormControl(variant = FormControlVariant.outlined) {
                                css{
                                    +StyleManager.defaultMarginTop
                                }
                                umInputLabel("${markLabel.text}",
                                    id = markLabel.id,
                                    error = markLabel.error,
                                    variant = InputLabelVariant.outlined,
                                    htmlFor = markLabel.id)
                                umOutlinedInput(
                                    id = markLabel.id,
                                    value = markGrade,
                                    label = markLabel.text,
                                    error = markLabel.error,
                                    type =  InputType.number,
                                    onChange = {
                                        setState {
                                            markGrade = it
                                            submitMarkError = null
                                        }
                                    }) {
                                    attrs.endAdornment = umTypography("/${entity?.block?.cbMaxPoints ?: 10}", variant = TypographyVariant.h6)

                                }
                                markLabel.errorText?.let { error ->
                                    umFormHelperText(error){
                                        css(StyleManager.errorTextClass)
                                    }
                                }
                            }
                        }


                        umItem(GridSize.cells12, GridSize.cells4) {
                            umButton(if(submissionScore == null)
                                getString(MessageID.submit_grade)
                                else getString(MessageID.update_grade),
                                variant = ButtonVariant.contained,
                                onClick = {
                                    if(markGrade.isNotEmpty()){
                                        mPresenter?.onClickSubmitGrade(markGrade.toFloat())
                                    }
                                }){
                                css {
                                    +StyleManager.defaultFullWidth
                                    +defaultMarginTop
                                    height = 50.px
                                }
                            }
                        }
                    }

                    if(markNextStudentVisible){
                        umItem(GridSize.cells12, GridSize.cells4) {
                            umButton(
                                if(submissionScore == null)
                                getString(MessageID.submit_grade_and_mark_next)
                                else getString(MessageID.update_grade_and_mark_next),
                                variant = ButtonVariant.contained,
                                onClick = {
                                    if(markGrade.isNotEmpty()){
                                        mPresenter?.onClickSubmitGradeAndMarkNext(markGrade.toFloat())
                                    }
                                }){
                                css {
                                    +StyleManager.defaultFullWidth
                                    +defaultMarginTop
                                    height = 50.px
                                }
                            }
                        }
                    }
                }

                if(entity?.caPrivateCommentsEnabled == true){
                    umItem(GridSize.cells12){
                        umGridContainer(rowSpacing = GridSpacing.spacing1) {

                            umItem(GridSize.cells12){
                                renderListSectionTitle(getString(MessageID.private_comments), TypographyVariant.h6)
                            }

                            umItem(GridSize.cells12) {
                                renderCreateNewComment(
                                    getString(MessageID.add_private_comment),
                                    mPresenter?.newPrivateCommentListener
                                )
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
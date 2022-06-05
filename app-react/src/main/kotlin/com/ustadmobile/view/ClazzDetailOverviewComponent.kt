package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorMediatorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.DraftJsUtil.clean
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.ASSET_ENTRY
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.formatDateRange
import com.ustadmobile.view.components.AttachmentImageLookupAdapter
import com.ustadmobile.view.ext.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzDetailOverviewComponent(mProps: UmProps): UstadDetailComponent<ClazzWithDisplayDetails>(mProps),
    ClazzDetailOverviewView {

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var schedules: List<Schedule> = listOf()

    private var courseBlocks: List<CourseBlockWithCompleteEntity> = listOf()

    private val scheduleObserver = ObserverFnWrapper<List<Schedule>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            schedules = it
        }
    }

    private val courseBlockObserver = ObserverFnWrapper<List<CourseBlockWithCompleteEntity>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            courseBlocks = it
        }
    }

    override var scheduleList: DoorDataSourceFactory<Int, Schedule>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(scheduleObserver)
            liveData?.observe(this, scheduleObserver)
        }

    override var courseBlockList: DoorDataSourceFactory<Int, CourseBlockWithCompleteEntity>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(courseBlockObserver)
            liveData?.observe(this, courseBlockObserver)
        }

    override var clazzCodeVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var showPermissionButton: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ClazzDetailOverviewPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +defaultPaddingTop
                +contentContainer
            }

            umGridContainer(columnSpacing = GridSpacing.spacing6) {
                umItem(GridSize.cells12, GridSize.cells4){
                    withAttachmentLocalUrlLookup(entity?.clazzUid ?: 0,
                        CLAZZ_PICTURE_LOOKUP_ADAPTER
                    ) { attachmentSrc ->
                        umEntityAvatar(listItem = true, fallbackSrc = ASSET_ENTRY, src = attachmentSrc)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells8){
                    umGridContainer {

                        umItem(GridSize.cells12){
                            umTypography(entity?.clazzDesc,
                                variant = TypographyVariant.body1,
                                gutterBottom = true){
                                css(StyleManager.alignTextToStart)
                            }

                        }

                        val numOfStudentTeachers = getString(MessageID.x_teachers_y_students)
                            .format(entity?.numTeachers ?: 0, entity?.numStudents ?: 0)

                        renderInformationOnDetailScreen("people", numOfStudentTeachers, getString(MessageID.members))

                        renderInformationOnDetailScreen("login", entity?.clazzCode ?: "", getString(MessageID.class_code)){
                            Util.copyToClipboard(entity?.clazzCode ?: "") {
                                showSnackBar(getString(MessageID.copied_to_clipboard))
                            }
                        }

                        renderInformationOnDetailScreen("school", entity?.clazzSchool?.schoolName)

                        renderInformationOnDetailScreen("event", entity?.clazzStartTime.formatDateRange(entity?.clazzEndTime))

                        renderInformationOnDetailScreen("event", entity?.clazzHolidayCalendar?.umCalendarName)

                        if(!schedules.isNullOrEmpty()){
                            umItem(GridSize.cells12){
                                css(defaultDoubleMarginTop)
                                renderListSectionTitle(getString(MessageID.schedule))
                            }

                            renderSchedules(schedules = schedules, withDelete = false)
                        }

                        umSpacer()

                        umGridContainer(GridSpacing.spacing4) {
                            if(!courseBlocks.isNullOrEmpty()){
                                renderCourseBlocks(blocks = courseBlocks){
                                    when(it.cbType){
                                        CourseBlock.BLOCK_MODULE_TYPE ->
                                            mPresenter?.handleModuleExpandCollapseClicked(it)
                                        CourseBlock.BLOCK_ASSIGNMENT_TYPE ->
                                            mPresenter?.handleClickAssignment(it.assignment as ClazzAssignment)
                                        CourseBlock.BLOCK_CONTENT_TYPE ->
                                            mPresenter?.contentEntryListItemListener?.onClickContentEntry(it.entry!!)
                                        CourseBlock.BLOCK_DISCUSSION_TYPE ->
                                            it.courseDiscussion?.let { it1 ->
                                                mPresenter?.handleClickCourseDiscussion(
                                                    it1
                                                )
                                            }
                                        CourseBlock.BLOCK_TEXT_TYPE ->
                                            mPresenter?.handleClickTextBlock(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    interface CourseBlockWithCompleteListProps: SimpleListProps<CourseBlockWithCompleteEntity>

    class CourseBlockWithCompleteListComponent(mProps: CourseBlockWithCompleteListProps): UstadSimpleList<CourseBlockWithCompleteListProps>(mProps){

        override fun RBuilder.renderListItem(item: CourseBlockWithCompleteEntity, onClick: (Event) -> Unit) {
            umGridContainer {
                attrs.onClick = {
                    Util.stopEventPropagation(it)
                    onClick.invoke(it.nativeEvent)
                }

                when(item.cbType){
                    in listOf(CourseBlock.BLOCK_MODULE_TYPE,CourseBlock.BLOCK_TEXT_TYPE) -> {
                        renderCourseBlockTextOrModuleListItem(
                            item.cbType,
                            item.cbIndentLevel,
                            item.cbTitle?: "",
                            showReorder = false,
                            withAction = item.cbType == CourseBlock.BLOCK_MODULE_TYPE,
                            actionIconName = if(item.expanded) "expand_less" else "expand_more",
                            description = clean(item.cbDescription ?: ""),
                            onActionClick = {
                                Util.stopEventPropagation(it)
                                onClick.invoke(it)
                            }
                        )
                    }
                    CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                        renderCourseBlockAssignment(item, systemImpl)
                    }

                    CourseBlock.BLOCK_CONTENT_TYPE -> {
                        item.entry?.let {
                            renderContentEntryListItem(it,systemImpl, showStatus= true, block = item)
                        }
                    }

                    CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                        renderCourseBlockTextOrModuleListItem(
                            item.cbType,
                            item.cbIndentLevel,
                            item.courseDiscussion?.courseDiscussionTitle?: "",
                            showReorder = false,
                            withAction = item.cbType == CourseBlock.BLOCK_DISCUSSION_TYPE,
                            description = clean(item.courseDiscussion?.courseDiscussionDesc ?: ""),
                            onActionClick = {
                                Util.stopEventPropagation(it)
                                onClick.invoke(it)
                            }
                        )
                    }
                }
            }
        }

    }

    private fun RBuilder.renderCourseBlocks(
        blocks: List<CourseBlockWithCompleteEntity>,
        createNewItem: CreateNewItem = CreateNewItem(),
        onEntryClicked: ((CourseBlockWithCompleteEntity) -> Unit)? = null
    ) = child(CourseBlockWithCompleteListComponent::class) {
        attrs.entries = blocks
        attrs.onEntryClicked = onEntryClicked
        attrs.createNewItem = createNewItem
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

    companion object {

        val CLAZZ_PICTURE_LOOKUP_ADAPTER = AttachmentImageLookupAdapter { db, entityUid ->
            object: DoorMediatorLiveData<String?>(), DoorObserver<CoursePicture?> {
                init {
                    addSource(db.coursePictureDao.findByClazzUidLive(entityUid), this)
                }

                override fun onChanged(t: CoursePicture?) {
                    postValue(t?.coursePictureUri)
                }
            }
        }

    }
}
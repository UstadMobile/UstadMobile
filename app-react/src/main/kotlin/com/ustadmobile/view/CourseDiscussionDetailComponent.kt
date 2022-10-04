package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseDiscussionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class CourseDiscussionDetailComponent(mProps: UmProps): UstadDetailComponent<CourseDiscussion>(mProps),
    CourseDiscussionDetailView {

    private var mPresenter: CourseDiscussionDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var topicList: List<DiscussionTopicListDetail> = listOf()


    private val topicsObserver = ObserverFnWrapper<List<DiscussionTopicListDetail>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            topicList = it
        }
    }

    override var topics: DataSourceFactory<Int, DiscussionTopicListDetail>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(topicsObserver)
            liveData?.observe(this, topicsObserver)
        }

    override var entity: CourseDiscussion? = null
        get() = field
        set(value) {
            ustadComponentTitle = value?.courseDiscussionTitle
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.submit)
        fabManager?.icon = "check"
        mPresenter = CourseDiscussionDetailPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
        fabManager?.onClickListener = {
            //mPresenter?.handleSubmitButtonClicked()
        }
    }



    override fun RBuilder.render() {
        if(entity == null) return
        styledDiv {
            css {
                +defaultDoubleMarginTop
                +contentContainer
            }

            umGridContainer(rowSpacing = GridSpacing.spacing3) {
                if(!entity?.courseDiscussionDesc.isNullOrBlank()){
                    umItem(GridSize.cells12){
                        umTypography(entity?.courseDiscussionDesc)
                    }
                }

                //Add topics heading
                renderListSectionTitle(getString(MessageID.topics))


                //Topics
                umItem(GridSize.cells12){
                    renderTopicListDetail(topicList)
                }

            }
        }

    }



    class TopicListDetailComponent(mProps: SimpleListProps<DiscussionTopicListDetail>): UstadSimpleList<SimpleListProps<DiscussionTopicListDetail>>(mProps){

        override fun RBuilder.renderListItem(item: DiscussionTopicListDetail, onClick: (Event) -> Unit) {
            umGridContainer {
                attrs.onClick = {
                    Util.stopEventPropagation(it)
                    onClick.invoke(it.nativeEvent)

                }

                renderListItemWithLeftIconTitleAndDescription(
                    "featured_play_list",
                    item.discussionTopicTitle?: "",
                    item.discussionTopicDesc?:"")
            }
        }

    }

    fun RBuilder.renderTopicListDetail(
        entries: List<DiscussionTopicListDetail>,
        onEntryClicked: ((DiscussionTopicListDetail) -> Unit)? = null
    ) = child(TopicListDetailComponent::class) {
        attrs.entries = entries
        attrs.hideDivider = true

        attrs.onEntryClicked = { topic ->
            mPresenter?.onClickTopic(topic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

    companion object {


    }
}
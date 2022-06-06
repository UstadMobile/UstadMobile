package com.ustadmobile.view

import com.ustadmobile.core.controller.DiscussionTopicDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.DiscussionTopicDetailView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.mui.components.FormControlComponent
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.fromNow
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class DiscussionTopicDetailComponent(mProps: UmProps): UstadDetailComponent<DiscussionTopic>(mProps),
    DiscussionTopicDetailView {

    private var mPresenter: DiscussionTopicDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var postList: List<DiscussionPostWithDetails> = listOf()


    private val postsObserver = ObserverFnWrapper<List<DiscussionPostWithDetails>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            postList = it
        }
    }

    override var posts: DoorDataSourceFactory<Int, DiscussionPostWithDetails>? = null
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(postsObserver)
            liveData?.observe(this, postsObserver)
        }

    override var entity: DiscussionTopic? = null
        get() = field
        set(value) {
            ustadComponentTitle = value?.discussionTopicTitle
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = true
        fabManager?.text = getString(MessageID.post)
        fabManager?.icon = "add"
        fabManager?.visible = true


        mPresenter = DiscussionTopicDetailPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())

        fabManager?.visible = true
        fabManager?.text = getString(MessageID.post)
        fabManager?.icon = "add"
        fabManager?.visible = true

        fabManager?.onClickListener = {
            mPresenter?.onClickAddPost()
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
                if(!entity?.discussionTopicDesc.isNullOrBlank()){
                    umItem(GridSize.cells12){
                        umTypography(entity?.discussionTopicDesc)
                    }
                }


                //Posts
                umItem(GridSize.cells12){
                    renderPostListDetail(postList)
                }

                //renderCreateNewItemOnList(getString(MessageID.post))

            }
        }

    }



    class PostListDetailComponent(mProps: SimpleListProps<DiscussionPostWithDetails>):
        UstadSimpleList<SimpleListProps<DiscussionPostWithDetails>>(mProps){

        override fun RBuilder.renderListItem(item: DiscussionPostWithDetails, onClick: (Event) -> Unit) {
            umGridContainer {
                attrs.onClick = {
                    Util.stopEventPropagation(it)
                    onClick.invoke(it.nativeEvent)
                }

                renderPostsDetail(
                    item.authorPersonFirstNames+" " + item.authorPersonLastName,
                    item.discussionPostMessage,
                    item.postLatestMessage,
                    item.postLatestMessageTimestamp.toDate()?.fromNow(systemImpl.getDisplayedLocale(this)),
                    item.postRepliesCount,
                    systemImpl
                )
            }
        }

    }

    private fun RBuilder.renderPostListDetail(
        entries: List<DiscussionPostWithDetails>,
        onEntryClicked: ((DiscussionPostWithDetails) -> Unit)? = null
    ) = child(PostListDetailComponent::class) {
        attrs.entries = entries
        attrs.hideDivider = true
        FormControlComponent.div
        attrs.onEntryClicked = { post ->
            mPresenter?.onClickPost(post as DiscussionPostWithDetails)
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
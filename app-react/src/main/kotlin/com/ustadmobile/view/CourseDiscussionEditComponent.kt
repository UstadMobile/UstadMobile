package com.ustadmobile.view

import com.ustadmobile.core.controller.CourseDiscussionEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.core.view.CourseDiscussionEditView
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import io.github.aakira.napier.Napier
import mui.material.FormControlVariant
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class CourseDiscussionEditComponent (mProps: UmProps): UstadEditComponent<CourseBlockWithEntity>(mProps),
    CourseDiscussionEditView {

    private var mPresenter: CourseDiscussionEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlockWithEntity>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))

    private var doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))


    private var topicsList: List<DiscussionTopic> = listOf()

    private val topicListObserver = ObserverFnWrapper<List<DiscussionTopic>> {
        setState {
            topicsList = it
        }
    }

    override var blockTitleError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                titleLabel = titleLabel.copy(errorText = field)
            }
        }

    override var startDate: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var startTime: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var topicList: MutableLiveData<List<DiscussionTopic>>? = null
        set(value) {
            field?.removeObserver(topicListObserver)
            field = value
            value?.observe(this, topicListObserver)
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: CourseBlockWithEntity? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = CourseDiscussionEditPresenter(this, arguments, this,this, di)
        setEditTitle(MessageID.add_discussion, MessageID.edit_discussion)
        Napier.d("CourseDiscussionEditComponent: navController viewName = ${navController.currentBackStackEntry?.viewName}" +
            "stateHandle=${(navController.currentBackStackEntry?.savedStateHandle as? UstadSavedStateHandleJs)?.dumpToString()}")
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +fieldsOnlyFormScreen
            }

            umItem(GridSize.cells12){
                umTextField(label = "${titleLabel.text}",
                    helperText = titleLabel.errorText,
                    value = entity?.courseDiscussion?.courseDiscussionTitle,
                    error = titleLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.courseDiscussion?.courseDiscussionTitle = it
                            blockTitleError = null
                        }
                    })
            }

            umItem(GridSize.cells12){
                umTextField(label = "${descriptionLabel.text}",
                    helperText = descriptionLabel.errorText,
                    value = entity?.courseDiscussion?.courseDiscussionDesc,
                    error = descriptionLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.courseDiscussion?.courseDiscussionDesc = it
                            blockTitleError = null
                        }
                    })
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells6){
                    umDatePicker(
                        label = "${doNotShowBeforeLabel.text}",
                        error = doNotShowBeforeLabel.error,
                        helperText = doNotShowBeforeLabel.errorText,
                        value = startDate.toDate(true),
                        inputVariant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                startDate = it.getTime().toLong()
                            }
                        }
                    )
                }

                umItem(GridSize.cells12, GridSize.cells6){
                    umTimePicker(
                        label = "${startTimeLabel.text}",
                        error = startTimeLabel.error,
                        helperText = startTimeLabel.errorText,
                        value = startDate.toDate(true),
                        inputVariant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                startDate = it.getTime().toLong()
                            }
                        }
                    )
                }
            }

            //Add topics heading
            renderListSectionTitle(getString(MessageID.topics))

            //Add Topics list
            val createTopic = CreateNewItem(true, getString(MessageID.add_topic)){
                setState {
                    showAddEntryOptions = true
                }
                mPresenter?.handleClickAddTopic()
            }

            mPresenter?.let { presenter ->

                renderTopics(presenter,topicsList.toSet().toList(),
                    createTopic,
                    onSortEnd = { fromIndex, toIndex ->
                        mPresenter?.onItemMove(fromIndex, toIndex)
                    }
                ){
                    mPresenter?.handleClickTopic(it)
                }
            }

            umSpacer()
            //Add Add Topic button
        }
    }

    private fun RBuilder.renderTopics(
        presenter: CourseDiscussionEditPresenter,
        blocks: List<DiscussionTopic>,
        createNewItem: CreateNewItem = CreateNewItem(),
        onSortEnd: (Int, Int) -> Unit,
        onEntryClicked: ((DiscussionTopic) -> Unit)? = null
    ) = child(DiscussionTopicListComponent::class) {
        attrs.entries = blocks
        attrs.presenter = presenter
        attrs.draggable = true
        attrs.onEntryClicked = onEntryClicked
        attrs.createNewItem = createNewItem
        attrs.onSortEnd = onSortEnd
    }

    interface DiscussionTopicListProps: SimpleListProps<DiscussionTopic>{
        var withDelete: Boolean
    }

    class DiscussionTopicListComponent(mProps: DiscussionTopicListProps):
        UstadSimpleList<DiscussionTopicListProps>(mProps){

        private var menuOptions: MutableList<ClazzEditComponent.CourseOption> = mutableListOf()

        private var showPopOverOptions = false

        private var anchorElement: Element? = null

        override fun RBuilder.renderMoreDialogOptions(){
            umMenu(showPopOverOptions,
                anchorElement = anchorElement,
                onClose = {
                    setState {
                        showPopOverOptions = false
                        anchorElement = null
                    }
                }) {

                menuOptions.filter{it.show}.forEach { option ->
                    umMenuItem("  ${getString(option.titleId)}  ",
                        onClick = {
                            option.onClick.invoke(it)
                            setState {
                                showPopOverOptions = false
                                anchorElement = null
                            }
                        }
                    )
                }
            }
        }

        override fun RBuilder.renderListItem(item: DiscussionTopic, onClick: (Event) -> Unit) {
            umGridContainer {
                attrs.onClick = {
                    Util.stopEventPropagation(it)
                    onClick.invoke(it.nativeEvent)
                }
                val presenter = props.presenter as CourseDiscussionEditPresenter


                renderListItemWithTitleDescriptionAndRightAction(
                    "${item.discussionTopicTitle}",
                    "delete", props.withDelete){
                    presenter.handleClickDeleteTopic(item)
                    //props.listener?.onClickDelete(item)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        blockTitleError = null
    }

}
package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder

interface CourseBlockListProps: SimpleListProps<CourseBlockWithEntity>

class CourseBlockListComponent(mProps: CourseBlockListProps): UstadSimpleList<CourseBlockListProps>(mProps){

    override fun RBuilder.renderListItem(item: CourseBlockWithEntity, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            /*createListItemWithTitleDescriptionAndRightAction(
                "$scheduleDays $startEndTime",
                "delete", props.withDelete){
                props.listener?.onClickDelete(item)
            }*/
        }
    }

}

fun RBuilder.renderCourseBlocks(
    presenter: UstadSingleEntityPresenter<*,*>?,
    blocks: List<CourseBlockWithEntity>,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((CourseBlockWithEntity) -> Unit)? = null
) = child(CourseBlockListComponent::class) {
    attrs.entries = blocks
    attrs.presenter = presenter
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
}
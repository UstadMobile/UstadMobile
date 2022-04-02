package com.ustadmobile.view

import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.renderContentEntryListItem
import com.ustadmobile.view.ext.renderCourseBlockAssignment
import com.ustadmobile.view.ext.renderCourseBlockTextOrModuleListItem
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder
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
                    renderCourseBlockTextOrModuleListItem(item.cbType, item.cbIndentLevel, item.cbTitle?: "",
                        showReorder = false,
                        withAction = item.cbType == CourseBlock.BLOCK_MODULE_TYPE && item.expanded,
                        actionIconName = if(item.expanded) "expand_less" else "expand_more",
                        description = item.cbDescription
                    )
                }
                CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                    renderCourseBlockAssignment(item, systemImpl)
                }

                CourseBlock.BLOCK_CONTENT_TYPE -> {
                    item.entry?.let {
                        renderContentEntryListItem(it, systemImpl)
                    }
                }
            }
        }
    }

}

fun RBuilder.renderCourseBlocksWithComplete(
    blocks: List<CourseBlockWithCompleteEntity>,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((CourseBlockWithCompleteEntity) -> Unit)? = null
) = child(CourseBlockWithCompleteListComponent::class) {
    attrs.entries = blocks
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
}
package com.ustadmobile.view

import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.createListItemWithTitleDescriptionAndRightAction
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder

interface ContentListProps: SimpleListProps<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>{
    var withDelete: Boolean
}

class ContentListComponent(mProps: ContentListProps): UstadSimpleList<ContentListProps>(mProps){

    override fun RBuilder.renderListItem(item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            createListItemWithTitleDescriptionAndRightAction(
                item.title ?: "",
                "delete", props.withDelete){
                props.listener?.onClickDelete(item)
            }
        }
    }

}

fun RBuilder.renderContentEntries(
    listener: OneToManyJoinEditListener<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null,
    contents: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>,
    withDelete: Boolean = true,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit)? = null
) = child(ContentListComponent::class) {
    attrs.entries = contents
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.listener = listener
    attrs.withDelete = withDelete
}
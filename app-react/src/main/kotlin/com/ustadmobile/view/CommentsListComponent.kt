package com.ustadmobile.view

import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.createListItemWithTitleDescriptionAndAvatarOnLeft
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder


class CommentsListComponent(mProps: ListProps<CommentsWithPerson>): UstadSimpleList<ListProps<CommentsWithPerson>>(mProps){

    override fun RBuilder.renderListItem(item: CommentsWithPerson, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            createListItemWithTitleDescriptionAndAvatarOnLeft(
                item.commentsPerson?.fullName() ?: "",
                item.commentsText,
                "person")
        }
    }

}

fun RBuilder.renderComments(
    entries: List<CommentsWithPerson>,
    onEntryClicked: ((CommentsWithPerson) -> Unit)? = null
) = child(CommentsListComponent::class) {
    attrs.entries = entries
    attrs.onEntryClicked = onEntryClicked
}
package com.ustadmobile.view

import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.mui.components.FormControlComponent
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.renderListItemWithPersonTitleDescriptionAndAvatarOnLeft
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder


class CommentsListComponent(mProps: SimpleListProps<CommentsWithPerson>): UstadSimpleList<SimpleListProps<CommentsWithPerson>>(mProps){

    override fun RBuilder.renderListItem(item: CommentsWithPerson, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            renderListItemWithPersonTitleDescriptionAndAvatarOnLeft(
                item.commentsPerson?.fullName() ?: "",
                item.commentsText,
                "person",
                systemImpl,
                accountManager,
                this
            )
        }
    }

}

fun RBuilder.renderComments(
    entries: List<CommentsWithPerson>,
    onEntryClicked: ((CommentsWithPerson) -> Unit)? = null
) = child(CommentsListComponent::class) {
    attrs.entries = entries
    attrs.hideDivider = true
    FormControlComponent.div
    attrs.onEntryClicked = onEntryClicked
}
package com.ustadmobile.view

import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.createListItemWithTitleDescriptionAndRightAction
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder

interface SiteTermsListPropsProps: SimpleListProps<SiteTermsWithLanguage>{
    var withDelete: Boolean
}

class SiteTermsListComponent(mProps: SiteTermsListPropsProps): UstadSimpleList<SiteTermsListPropsProps>(mProps){

    override fun RBuilder.renderListItem(item: SiteTermsWithLanguage, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            createListItemWithTitleDescriptionAndRightAction(
                item.stLanguage?.name ?: "",
                "delete", props.withDelete){
                props.listener?.onClickDelete(item)
            }
        }
    }

}

fun RBuilder.renderSiteTerms(
    listener: OneToManyJoinEditListener<SiteTermsWithLanguage>? = null,
    terms: List<SiteTermsWithLanguage>,
    withDelete: Boolean = true,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((SiteTermsWithLanguage) -> Unit)? = null
) = child(SiteTermsListComponent::class) {
    attrs.entries = terms
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.listener = listener
    attrs.withDelete = withDelete
}
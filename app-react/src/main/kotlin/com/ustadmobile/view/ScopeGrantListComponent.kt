package com.ustadmobile.view

import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ScopedGrantOneToManyHelper
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.view.ext.renderItemWithLeftIconTitleDescriptionAndIconBtnOnRight
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder


class ScopeGrantListComponent(mProps: SimpleListProps<ScopedGrantAndName>): UstadSimpleList<SimpleListProps<ScopedGrantAndName>>(mProps){

    override fun RBuilder.renderListItem(item: ScopedGrantAndName, onClick: (Event) -> Unit) {
        val showDelete = item.scopedGrant?.sgFlags?.hasFlag(ScopedGrant.FLAG_NO_DELETE) == false
        val permissionList = permissionListText(systemImpl, Clazz.TABLE_ID,
            item.scopedGrant?.sgPermissions ?: 0)
        if(showDelete){
            renderItemWithLeftIconTitleDescriptionAndIconBtnOnRight("admin_panel_settings",
                "delete",item.name, permissionList){ delete, event ->
                if(delete){
                    props.listener?.onClickDelete(item)
                }else {
                    onClick.invoke(event)
                }
            }
        }else{
            umGridContainer {
                attrs.onClick = {
                    onClick.invoke(it.nativeEvent)
                }
                renderListItemWithLeftIconTitleAndDescription("admin_panel_settings",
                    item.name, permissionList)
            }
        }
    }

}

fun RBuilder.permissionListText(
    systemImpl: UstadMobileSystemImpl,
    tableId: Int, bitmaskValue: Long): String? {

    val flagMessageIds = ScopedGrantEditPresenter.PERMISSION_LIST_MAP[tableId]
    return flagMessageIds?.map { it.toBitmaskFlag(bitmaskValue) }
        ?.filter { it.enabled }
        ?.joinToString { systemImpl.getString(it.messageId, this) }
}


fun RBuilder.renderScopedGrants(
    listener: ScopedGrantOneToManyHelper,
    scopes: List<ScopedGrantAndName>,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((ScopedGrantAndName) -> Unit)? = null
) = child(ScopeGrantListComponent::class) {
    attrs.entries = scopes
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.listener = listener
}
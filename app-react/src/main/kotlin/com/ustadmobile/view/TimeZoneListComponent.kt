package com.ustadmobile.view

import com.ustadmobile.core.controller.OnSearchSubmitted
import com.ustadmobile.core.controller.TimeZoneListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.TimeZoneListView
import com.ustadmobile.util.TimeZone
import com.ustadmobile.util.TimeZonesUtil
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import com.ustadmobile.view.ext.umGridContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState

class TimeZoneListComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps) , TimeZoneListView,
    OnSearchSubmitted {

    private var mPresenter: TimeZoneListPresenter? = null

    private var timeZoneList: List<TimeZone> = TimeZonesUtil.getTimeZones()

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.timezone)
        mPresenter = TimeZoneListPresenter(this, arguments,
            this, di)
        searchManager?.searchListener = this
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        renderZoneList(timeZoneList){ timezone ->
            mPresenter?.handleClickTimeZone(timezone.id)
        }
    }

    override fun onSearchSubmitted(text: String?) {
        if(text == null){
            return
        }

        GlobalScope.launch {
            val searchWords = text.split(Regex("\\s+"))
            val filteredItems = TimeZonesUtil.getTimeZones().filter { timeZone ->
                searchWords.any { timeZone.id.contains(it, ignoreCase = true) }
                        || searchWords.any { timeZone.timeName.contains(it, ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
               setState {
                   timeZoneList = filteredItems
               }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}

class ZonesListComponent(mProps: SimpleListProps<TimeZone>):
    UstadSimpleList<SimpleListProps<TimeZone>>(mProps){
    override fun RBuilder.renderListItem(item: TimeZone, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                onClick.invoke(it.nativeEvent)
            }
            renderListItemWithLeftIconTitleAndDescription("query_builder",item.name,
                item.timeName, onMainList = true)
        }
    }
}

fun RBuilder.renderZoneList(
    zones: List<TimeZone>,
    onEntryClicked: ((TimeZone) -> Unit)? = null
) = child(ZonesListComponent::class) {
    attrs.entries = zones
    attrs.onEntryClicked = onEntryClicked
    attrs.mainList = true
}
package com.ustadmobile.view

import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.formattedInHoursAndMinutes
import com.ustadmobile.view.ext.renderListItemWithTitleDescriptionAndRightAction
import com.ustadmobile.view.ext.umGridContainer
import org.w3c.dom.events.Event
import react.RBuilder
import kotlin.js.Date

interface ScheduleListProps: SimpleListProps<Schedule>{
    var withDelete: Boolean
}

class ScheduleListComponent(mProps: ScheduleListProps): UstadSimpleList<ScheduleListProps>(mProps){

    override fun RBuilder.renderListItem(item: Schedule, onClick: (Event) -> Unit) {
        umGridContainer {
            attrs.onClick = {
                Util.stopEventPropagation(it)
                onClick.invoke(it.nativeEvent)
            }

            val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
                .firstOrNull { it.optionVal == item.scheduleFrequency }?.messageId ?: MessageID.None
            val dayMessageId = ScheduleEditPresenter.DayOptions.values()
                .firstOrNull { it.optionVal == item.scheduleDay }?.messageId ?: MessageID.None

            val scheduleDays = "${systemImpl.getString(frequencyMessageId, this)} - ${systemImpl.getString(dayMessageId, this)}"

            val startEndTime = "${Date(item.sceduleStartTime).formattedInHoursAndMinutes()} " +
                    "- ${Date(item.scheduleEndTime).formattedInHoursAndMinutes()}"

            renderListItemWithTitleDescriptionAndRightAction(
                "$scheduleDays $startEndTime",
                "delete", props.withDelete){
                props.listener?.onClickDelete(item)
            }
        }
    }

}

fun RBuilder.renderSchedules(
    listener: OneToManyJoinEditListener<Schedule>? = null,
    schedules: List<Schedule>,
    withDelete: Boolean = true,
    createNewItem: CreateNewItem = CreateNewItem(),
    onEntryClicked: ((Schedule) -> Unit)? = null
) = child(ScheduleListComponent::class) {
    attrs.entries = schedules
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.listener = listener
    attrs.withDelete = withDelete
}
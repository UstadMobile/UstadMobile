package com.ustadmobile.view

import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.ext.formattedInHoursAndMinutes
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import styled.css
import kotlin.js.Date


class ScheduleListComponent(mProps: ListProps<Schedule>): UstadSimpleList<ListProps<Schedule>>(mProps){

    override fun RBuilder.renderListItem(item: Schedule) {
        umGridContainer {
            val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
                .firstOrNull { it.optionVal == item.scheduleFrequency }?.messageId ?: MessageID.None
            val dayMessageId = ScheduleEditPresenter.DayOptions.values()
                .firstOrNull { it.optionVal == item.scheduleDay }?.messageId ?: MessageID.None

            val scheduleDays = "${systemImpl.getString(frequencyMessageId, this)} - ${systemImpl.getString(dayMessageId, this)}"

            val startEndTime = "${Date(item.sceduleStartTime).formattedInHoursAndMinutes()} " +
                    "- ${Date(item.scheduleEndTime).formattedInHoursAndMinutes()}"

            umItem(GridSize.cells10, GridSize.cells11){
                umTypography("$scheduleDays $startEndTime",
                    variant = TypographyVariant.body2,
                    gutterBottom = true){
                    css(StyleManager.alignTextToStart)
                }
            }

            umItem(GridSize.cells2, GridSize.cells1){
                umIconButton("delete",
                    size = IconButtonSize.small,
                    onClick = {
                    props.listener.onClickDelete(item)
                })
            }
        }
    }

}

fun RBuilder.renderSchedules(listener: OneToManyJoinEditListener<Schedule>,
                             schedules: List<Schedule>,
                             createNewItem: CreateNewItem = CreateNewItem(),
                             onEntryClicked: ((Schedule) -> Unit)? = null) = child(ScheduleListComponent::class) {
    attrs.entries = schedules
    attrs.onEntryClicked = onEntryClicked
    attrs.createNewItem = createNewItem
    attrs.listener = listener
}
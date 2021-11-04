package com.ustadmobile.view

import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.MTypographyColor
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.button.MIconButtonSize
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.mTypography
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.Schedule
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

            umItem(MGridSize.cells10, MGridSize.cells11){
                mTypography("$scheduleDays $startEndTime",
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary,
                    gutterBottom = true){
                    css(StyleManager.alignTextToStart)
                }
            }

            umItem(MGridSize.cells2, MGridSize.cells1){
                mIconButton("delete", size = MIconButtonSize.small, onClick = {
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
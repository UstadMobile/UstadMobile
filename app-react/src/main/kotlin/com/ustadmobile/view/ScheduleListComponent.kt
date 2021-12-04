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
import kotlinx.css.LinearDimension
import kotlinx.css.display
import kotlinx.css.paddingTop
import org.w3c.dom.events.Event
import react.RBuilder
import styled.css
import kotlin.js.Date

interface ScheduleListProps: ListProps<Schedule>{
    var withDelete: Boolean
}

class ScheduleListComponent(mProps: ScheduleListProps): UstadSimpleList<ScheduleListProps>(mProps){

    override fun RBuilder.renderListItem(item: Schedule, onClick: (Event) -> Unit) {
        umGridContainer {
            val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
                .firstOrNull { it.optionVal == item.scheduleFrequency }?.messageId ?: MessageID.None
            val dayMessageId = ScheduleEditPresenter.DayOptions.values()
                .firstOrNull { it.optionVal == item.scheduleDay }?.messageId ?: MessageID.None

            val scheduleDays = "${systemImpl.getString(frequencyMessageId, this)} - ${systemImpl.getString(dayMessageId, this)}"

            val startEndTime = "${Date(item.sceduleStartTime).formattedInHoursAndMinutes()} " +
                    "- ${Date(item.scheduleEndTime).formattedInHoursAndMinutes()}"

            umItem(if(props.withDelete) GridSize.cells10 else GridSize.cells12, if(props.withDelete) GridSize.cells11 else GridSize.cells12){
                attrs.onClick = {
                    onClick.invoke(it.nativeEvent)
                }
                css{
                    paddingTop = LinearDimension("8px")
                }
                umTypography("$scheduleDays $startEndTime",
                    variant = TypographyVariant.body2,
                    gutterBottom = true){
                    css(StyleManager.alignTextToStart)
                }
            }

            umItem(GridSize.cells2, GridSize.cells1){
                css{
                    display = StyleManager.displayProperty(props.withDelete)
                }
                umIconButton("delete",
                    size = IconButtonSize.small,
                    onClick = {
                        it.stopPropagation()
                        props.listener?.onClickDelete(item)
                    })
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
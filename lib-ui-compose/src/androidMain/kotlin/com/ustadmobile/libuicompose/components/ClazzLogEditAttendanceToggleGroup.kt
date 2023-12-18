package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import com.google.android.material.button.MaterialButtonToggleGroup
import android.view.LayoutInflater
import android.widget.Button
import androidx.compose.ui.viewinterop.AndroidView
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.libuicompose.R

private val buttonsIdMap = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
    ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button
)

@Composable
actual fun ClazzLogEditAttendanceToggleGroup(
    isEnabled: Boolean,
    attendanceStatus: Int,
    onAttendanceStatusChanged: (Int) -> Unit
) {

    fun MaterialButtonToggleGroup.update() {
        buttonsIdMap.forEach { (status, buttonId) ->
            val button = findViewById<Button>(buttonId)
            button.isEnabled = isEnabled

            button.setOnClickListener {
                onAttendanceStatusChanged(status)
            }
        }

        val idToCheck = buttonsIdMap[attendanceStatus]
        if(idToCheck != null) {
            check(idToCheck)
        }else {
            clearChecked()
        }
    }

    AndroidView(
        factory = {  context ->
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_clazz_log_attendance_status_toggle_buttons,
                null, false
            ) as MaterialButtonToggleGroup

            view.isSingleSelection = true
            view.update()


            view
        },
        update = {
            it.update()
        }
    )

}
package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.core.R as CR

class SchoolEditFragment: UstadBaseMvvmFragment() {

}

@Composable
private fun SchoolEditScreen(
    uiState: SchoolEditUiState = SchoolEditUiState(),
    onSchoolChanged: (SchoolWithHolidayCalendar?) -> Unit = {},
    onClickTimeZone: () -> Unit = {},
    onClickHolidayCalendar: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolName ?: "",
                label = stringResource(id = CR.string.name_key),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolName = it
                    })
                },
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolDesc ?: "",
                label = stringResource(id = CR.string.description),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolDesc = it
                    })
                },
            )
        }


        item {
            UstadTextEditField(
                value = uiState.entity?.schoolTimeZone ?: "",
                label = stringResource(id = CR.string.timezone),
                enabled = uiState.fieldsEnabled,
                readOnly = true,
                onClick = onClickTimeZone,
                onValueChange = {  }
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.holidayCalendar?.umCalendarName ?: "",
                label = stringResource(id = CR.string.holiday_calendar),
                enabled = uiState.fieldsEnabled,
                readOnly = true,
                onClick = onClickHolidayCalendar,
                onValueChange = {  }
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolAddress ?: "",
                label = stringResource(id = CR.string.address),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolAddress = it
                    })
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolPhoneNumber ?: "",
                label = stringResource(id = CR.string.phone_number),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolPhoneNumber = it
                    })
                },
            )
        }

        item {
            UstadTextEditField(
                value = uiState.entity?.schoolEmailAddress ?: "",
                label = stringResource(id = CR.string.email),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onSchoolChanged(uiState.entity?.shallowCopy{
                        schoolEmailAddress = it
                    })
                },
            )
        }
    }
}


@Composable
@Preview
fun SchoolEditScreenPreview() {
    val uiState = SchoolEditUiState(
        entity = SchoolWithHolidayCalendar().apply {
            schoolName = "School A"
            schoolDesc = "This is a test school"
            schoolTimeZone = "Asia/Dubai"
            schoolAddress = "123, Main Street, Nairobi, Kenya"
            schoolPhoneNumber = "+90012345678"
            schoolEmailAddress = "info@schoola.com"
        },
    )
    MdcTheme {
        SchoolEditScreen(uiState)
    }
}
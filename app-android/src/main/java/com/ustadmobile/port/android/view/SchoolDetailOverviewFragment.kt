package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.core.R as CR

interface SchoolDetailOverviewEventListener {
    fun onClickSchoolCode(code: String?)
}

class SchoolDetailOverviewFragment: UstadBaseMvvmFragment() {


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SchoolDetailOverviewScreen(
    uiState: SchoolDetailOverviewUiState = SchoolDetailOverviewUiState(),
    onClickSchoolCode: () -> Unit = {},
    onClickSchoolPhoneNumber: () -> Unit = {},
    onClickSms: () -> Unit = {},
    onClickEmail: () -> Unit = {},
    onClickClazz: (Clazz) -> Unit = {},
) {
    LazyColumn {

        if (uiState.schoolDescVisible){
           item {
               Text(
                   modifier = Modifier.padding(all = 16.dp),
                   text = uiState.entity?.schoolDesc ?: "",
                   style = Typography.h6
               )
           }
        }

        if (uiState.schoolCodeLayoutVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolCode ?: "",
                    labelText = stringResource(id = CR.string.school_code),
                    imageId = R.drawable.ic_login_24px,
                    onClick = onClickSchoolCode
                )
            }
        }

        if (uiState.schoolAddressVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolAddress ?: "",
                    labelText = stringResource(id = CR.string.address),
                    imageId = R.drawable.ic_location_pin_24dp
                )
            }
        }

        if (uiState.schoolPhoneNumberVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolPhoneNumber ?: "",
                    labelText = stringResource(id = CR.string.phone_number),
                    imageId = R.drawable.ic_call_bcd4_24dp,
                    onClick = onClickSchoolPhoneNumber,
                    secondaryActionContent = {
                        IconButton(
                            onClick = onClickSms,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Message,
                                contentDescription = stringResource(id = CR.string.message),
                            )
                        }
                    }
                )
            }
        }

        if (uiState.calendarUidVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.holidayCalendar?.umCalendarName ?: "",
                    labelText = stringResource(id = CR.string.holiday_calendar),
                    imageId = R.drawable.ic_perm_contact_calendar_black_24dp
                )
            }
        }

        if (uiState.schoolEmailAddressVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolAddress ?: "",
                    labelText = stringResource(id = CR.string.email),
                    imageId = R.drawable.ic_email_black_24dp,
                    onClick = onClickEmail
                )
            }
        }

        if (uiState.schoolTimeZoneVisible){
            item {
                UstadDetailField(
                    valueText = uiState.entity?.schoolTimeZone ?: "",
                    labelText = stringResource(id = CR.string.timezone),
                    imageId = R.drawable.ic_language_blue_grey_600_24dp,
                )
            }
        }

        item {
            Text(text = stringResource(id = CR.string.courses),
                style = Typography.h6
            )
        }


        items(
            items = uiState.clazzes,
            key = { clazz -> clazz.clazzUid }
        ){ clazz ->
            ListItem(
                modifier = Modifier.clickable {
                    onClickClazz(clazz)
                },
                text = { Text(clazz.clazzName ?: "") },
                secondaryText = { Text(clazz.clazzDesc ?: "") }
            )
        }
    }
}

@Composable
@Preview
fun SchoolDetailOverviewScreenPreview() {
    val uiStateVal = SchoolDetailOverviewUiState(
        entity = SchoolWithHolidayCalendar().apply {
            schoolDesc = "School description over here."
            schoolCode = "abc123"
            schoolAddress = "Nairobi, Kenya"
            schoolPhoneNumber = "+971 44311111"
            schoolGender = 1
            schoolHolidayCalendarUid = 1
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "Kenya calendar A"
            }
            schoolEmailAddress = "info@schoola.com"
            schoolTimeZone = "Asia/Dubai"
        },
        schoolCodeVisible = true,
        clazzes = listOf(
            ClazzWithListDisplayDetails().apply {
                clazzUid = 0L
                clazzName = "Class A"
                clazzDesc = "Class description"
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 1L
                clazzName = "Class B"
                clazzDesc = "Class description"
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 2L
                clazzName = "Class C"
                clazzDesc = "Class description"
            }
        )
    )

    MdcTheme {
        SchoolDetailOverviewScreen(uiStateVal)
    }
}
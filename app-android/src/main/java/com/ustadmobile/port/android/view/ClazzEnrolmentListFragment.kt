package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListItemUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.util.compose.rememberFormattedDateRange
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton


class ClazzEnrolmentListFragment(): UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzEnrolmentListViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzEnrolmentListScreen(viewModel)
                }
            }
        }
    }

}

@Composable
fun ClazzEnrolmentListScreen(
    viewModel: ClazzEnrolmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzEnrolmentListUiState())
    ClazzEnrolmentListScreen(
        uiState = uiState,
        onEditItemClick = viewModel::onClickEditEnrolment,
        onViewProfileClick = viewModel::onClickViewProfile,
    )
}

@Composable
fun ClazzEnrolmentListScreen(
    uiState: ClazzEnrolmentListUiState,
    onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit = {},
    onViewProfileClick: () -> Unit = {}
){
    val courseTerminologyEntries = rememberCourseTerminologyEntries(uiState.courseTerminology)

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ){

        item{
            UstadQuickActionButton(
                labelText = stringResource(id = R.string.view_profile),
                imageId = R.drawable.ic_person_black_24dp,
                onClick = onViewProfileClick
            )
        }

        item {
            Divider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        }

        item{
            Text(
                text = stringResource(id = R.string.person_enrolment_in_class,
                    uiState.personName ?: "", uiState.courseName ?: ""),
                style = Typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }

        items(
            uiState.enrolmentList,
            key = {
                it.clazzEnrolmentUid
            }
        ){  enrolment ->
            ClazzEnrolmentListItem(
                uiState = uiState.enrolmentItemUiState(enrolment),
                onEditItemClick = onEditItemClick,
                terminologyEntries = courseTerminologyEntries,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzEnrolmentListItem(
    uiState: ClazzEnrolmentListItemUiState,
    onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit,
    terminologyEntries: List<TerminologyEntry>,
) {
    val enrolment = uiState.enrolment

    val joinedLeftDate = rememberFormattedDateRange(
        startTimeInMillis = enrolment.clazzEnrolmentDateJoined,
        endTimeInMillis = enrolment.clazzEnrolmentDateLeft,
        timeZoneId = uiState.timeZone,
    )

    val itemPrimaryText = buildString {
        val roleMessageId = ClazzEnrolmentListConstants
            .ROLE_TO_STRING_RESOURCE_MAP[enrolment.clazzEnrolmentRole]
            ?: ClazzEnrolment.ROLE_STUDENT
        val outcomeMessageId = ClazzEnrolmentListConstants
            .OUTCOME_TO_STRING_RESOURCE_MAP[enrolment.clazzEnrolmentOutcome] ?: 0

        append(courseTerminologyEntryResource(terminologyEntries, roleMessageId))
        append(" - ")
        append(messageIdResource(id =  outcomeMessageId))
        if (enrolment.leavingReason != null){
            append(" (")
            append(enrolment.leavingReason?.leavingReasonTitle ?: "")
            append(")")
        }
    }




    ListItem(
        text = { Text(text = itemPrimaryText) },
        secondaryText = { Text(text = joinedLeftDate)},
        trailing = {
            if(uiState.canEdit) {
                IconButton(
                    onClick = {
                        onEditItemClick(enrolment)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_white_24dp),
                        contentDescription = stringResource(id = R.string.edit)
                    )
                }
            }
        }
    )
}

@Composable
@Preview
fun ClazzEnrolmentListPreview(){
    ClazzEnrolmentListScreen(
        uiState = ClazzEnrolmentListUiState(
            personName = "Ahmad",
            courseName = "Mathematics",
            enrolmentList = listOf(
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349880298
                    clazzEnrolmentDateLeft = 509823093
                    clazzEnrolmentUid = 7
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 201
                },
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349887338
                    clazzEnrolmentDateLeft = 409937093
                    clazzEnrolmentUid = 8
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 203
                    leavingReason = LeavingReason().apply {
                        leavingReasonTitle = "transportation problem"
                    }
                }
            )
        )
    )
}
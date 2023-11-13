package com.ustadmobile.port.android.view.coursegroupset.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.core.R as CR


class CourseGroupSetDetailFragment: UstadBaseMvvmFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

                }
            }
        }
    }


}
@Composable
private fun CourseGroupSetDetailScreen(
    viewModel: CourseGroupSetDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetDetailUiState())
    CourseGroupSetDetailScreen(uiState = uiState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetDetailScreen(
    uiState: CourseGroupSetDetailUiState
){
    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        (1..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).forEach { groupNum ->
            val members = uiState.membersList.filter { it.cgm?.cgmGroupNumber == groupNum }

            if(members.isNotEmpty()) {
                item(key = "header_${groupNum}") {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "${stringResource(id = CR.string.group)} $groupNum",
                        style = Typography.body1
                    )
                }

                items(
                    items = members,
                    key = { it.personUid }
                ){
                    ListItem(
                        text = {
                            Text(text = "${it.name}")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun CourseGroupSetDetailScreenPreview(){
    CourseGroupSetDetailScreen(
        uiState = CourseGroupSetDetailUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "Group 1"
                cgsTotalGroups = 4
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    )
}
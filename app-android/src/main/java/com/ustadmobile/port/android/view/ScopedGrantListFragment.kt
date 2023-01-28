package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.controller.ScopedGrantListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantListView
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.core.controller.UstadListPresenter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.PermissionConstants
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.viewmodel.ScopedGrantListUiState
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class ScopedGrantListFragment(
): UstadListViewFragment<ScopedGrant, ScopedGrantWithName>(),
        ScopedGrantListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ScopedGrantListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ScopedGrantWithName>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ScopedGrantListPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = ScopedGrantListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add))
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.permission)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.scopedGrantDao

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScopedGrantListScreen(
    uiState: ScopedGrantListUiState = ScopedGrantListUiState(),
    onClickScopedGrant: (ScopedGrantWithName) -> Unit = {},
    onClickAddScopedGrant: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    )  {

        if(uiState.addMode == ListViewAddMode.FIRST_ITEM) {
            item {
                UstadAddListItem(
                    text = stringResource(id = R.string.add),
                    onClickAdd = { onClickAddScopedGrant() }
                )
            }
        }

        items(
            items = uiState.scopedGrantList,
            key = { scopedGrant -> scopedGrant.sgUid }
        ){ scopedGrant ->

            val permissions = PermissionConstants.PERMISSION_MESSAGE_IDS
                .filter{
                    scopedGrant.sgPermissions.hasFlag(it.flagVal)}
                .map {
                    messageIdResource(it.messageId) }.joinToString()

            ListItem(
                modifier = Modifier.clickable {
                    onClickScopedGrant(scopedGrant)
                },
                icon = {
                    Spacer(modifier = Modifier.width(24.dp))
                },
                text = { Text(scopedGrant.name ?: "") },
                secondaryText = { Text(text = permissions) }
            )
        }
    }
}

@Composable
@Preview
fun ScopedGrantListScreenPreview() {
    MdcTheme {
        ScopedGrantListScreen(
            uiState = ScopedGrantListUiState(
                scopedGrantList = listOf(
                    ScopedGrantWithName().apply {
                        sgUid = 1
                        name = "First Item"
                        sgPermissions = Role.PERMISSION_PERSON_DELEGATE+Role.PERMISSION_SCHOOL_UPDATE
                    },
                    ScopedGrantWithName().apply {
                        sgUid = 2
                        name = "Second Item"
                        sgPermissions = Role.PERMISSION_PERSON_DELEGATE+
                                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE
                    },
                    ScopedGrantWithName().apply {
                        sgUid = 3
                        name = "Third Item"
                    }
                )
            )
        )
    }
}
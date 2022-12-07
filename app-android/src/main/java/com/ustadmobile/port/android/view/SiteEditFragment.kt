package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteEditBinding
import com.toughra.ustadmobile.databinding.ItemSiteTermsEditBinding
import com.ustadmobile.core.controller.SiteEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteEditView
import com.ustadmobile.core.viewmodel.SiteDetailUiState
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadEditField
import com.ustadmobile.port.android.view.composable.UstadSwitchField
import com.ustadmobile.port.android.view.composable.UstadTextEditField


class SiteEditFragment: UstadEditFragment<Site>(), SiteEditView {

    private var mBinding: FragmentSiteEditBinding? = null

    private var mPresenter: SiteEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Site>?
        get() = mPresenter


    class SiteTermsRecyclerAdapter(var presenter: SiteEditPresenter?): ListAdapter<SiteTermsWithLanguage, SiteTermsRecyclerAdapter.SiteTermsViewHolder>(DIFF_CALLBACK_WORKSPACETERMS) {

            class SiteTermsViewHolder(val binding: ItemSiteTermsEditBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteTermsViewHolder {
                val viewHolder = SiteTermsViewHolder(ItemSiteTermsEditBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                return viewHolder
            }

            override fun onBindViewHolder(holder: SiteTermsViewHolder, position: Int) {
                holder.binding.siteTerms = getItem(position)
            }
        }

    override var siteTermsList: LiveData<List<SiteTermsWithLanguage>>? = null
        get() = field
        set(value) {
            field?.removeObserver(workspaceTermsObserver)
            field = value
            value?.observe(this, workspaceTermsObserver)
        }

    private var siteTermsRecyclerAdapter: SiteTermsRecyclerAdapter? = null

    //private var workspaceTermsRecyclerView: RecyclerView? = null

    private val workspaceTermsObserver = Observer<List<SiteTermsWithLanguage>?> {
        t -> siteTermsRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteEditBinding.inflate(inflater, container, false).also {
            rootView = it.root

            siteTermsRecyclerAdapter = SiteTermsRecyclerAdapter(mPresenter)
            it.siteTermsRv.adapter = siteTermsRecyclerAdapter
            it.siteTermsRv.layoutManager = LinearLayoutManager(requireContext())
            it.mPresenter = mPresenter
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        siteTermsRecyclerAdapter?.presenter = mPresenter

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
    }

    override var entity: Site? = null
        get() = field
        set(value) {
            field = value
            mBinding?.site = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {

        val DIFF_CALLBACK_WORKSPACETERMS = object: DiffUtil.ItemCallback<SiteTermsWithLanguage>() {
            override fun areItemsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsUid == newItem.sTermsUid
            }

            override fun areContentsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsLang == newItem.sTermsLang
                        && oldItem.termsHtml == newItem.termsHtml
            }
        }

    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SiteEditScreen(
    uiState: SiteEditUiState,
    onSiteChanged: (Site?) -> Unit = {},
    onItemClicked: (SiteTermsWithLanguage) -> Unit = {},
    onDeleteIconClicked: (SiteTermsWithLanguage) -> Unit = {},
    onClickAddItem: () -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        UstadTextEditField(
            value = uiState.site?.siteName ?: "",
            label = stringResource(id = R.string.name),
            error = uiState.siteNameError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onSiteChanged(uiState.site?.shallowCopy{
                    siteName = it
                })
            }
        )

        UstadSwitchField(
            modifier = Modifier.padding(top = 10.dp),
            checked = uiState.site?.guestLogin ?: false,
            label = stringResource(id = R.string.guest_login_enabled),
            onChange = {
                onSiteChanged(uiState.site?.shallowCopy {
                    guestLogin = it
                })
            }
        )

        UstadSwitchField(
            modifier = Modifier.padding(top = 15.dp),
            checked = uiState.site?.registrationAllowed ?: false,
            label = stringResource(id = R.string.registration_allowed),
            onChange = {
                onSiteChanged(uiState.site?.shallowCopy {
                    registrationAllowed = it
                })
            }
        )

        Text(
            stringResource(R.string.terms_and_policies),
            style = Typography.h6,
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickAddItem()
            },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )
            },
            text = {Text(stringResource(id = R.string.terms_and_policies))}
        )

        uiState.siteTerms.forEach {item ->
            ListItem(
                modifier = Modifier.clickable {
                    onItemClicked(item)
                },
                text = {Text(item.stLanguage?.name ?: "")},
                        trailing = {
                    IconButton(onClick = {
                        onDeleteIconClicked(item)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        }

    }
}

@Composable
@Preview
fun SiteEditScreenPreview(){
    SiteEditScreen(
        uiState = SiteEditUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsWithLanguage().apply {
                    stLanguage = Language().apply {
                        name = "fa"
                    }
                }
            )
        ),
    )
}
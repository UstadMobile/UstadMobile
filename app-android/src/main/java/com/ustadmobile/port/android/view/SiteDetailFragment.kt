package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteDetailBinding
import com.toughra.ustadmobile.databinding.ItemSiteBinding
import com.toughra.ustadmobile.databinding.ItemSiteTermsBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.SiteDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.SiteDetailUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.port.android.view.util.ListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography


interface WorkspaceDetailFragmentEventHandler {

}

class SiteDetailFragment: UstadDetailFragment<Site>(), SiteDetailView, WorkspaceDetailFragmentEventHandler {

    class SiteViewHolder(val mBinding: ItemSiteBinding): RecyclerView.ViewHolder(mBinding.root)

    class SiteRecyclerViewAdapter : ListAdapter<Site, SiteViewHolder>(DIFFUTIL_SITE) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
            return SiteViewHolder(ItemSiteBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }

        override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
            holder.mBinding.site = getItem(position)
        }
    }

    class SiteTermsViewHolder(val mBinding: ItemSiteTermsBinding): RecyclerView.ViewHolder(mBinding.root)

    inner class SiteTermsRecyclerViewAdapter: ListAdapter<SiteTermsWithLanguage,SiteTermsViewHolder>(DIFFUTIL_SITE_TERMS) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteTermsViewHolder {
            return SiteTermsViewHolder(ItemSiteTermsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: SiteTermsViewHolder, position: Int) {
            holder.mBinding.siteTermsWithLanguage = getItem(position)
            holder.mBinding.presenter = mPresenter
        }
    }


    private var mBinding: FragmentSiteDetailBinding? = null

    private var mPresenter: SiteDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var siteRecyclerViewAdapter: SiteRecyclerViewAdapter? = null

    private var mSiteTermsRecyclerViewAdapter: SiteTermsRecyclerViewAdapter? = null

    private var mMergeAdapter: ConcatAdapter? = null

    override var entity: Site? = null
        set(value) {
            field = value

            siteRecyclerViewAdapter?.submitList(if(value != null) {
                listOf(value)
            }else {
                listOf()
            })
        }


    private var currentSiteTermsLiveData: LiveData<PagedList<SiteTermsWithLanguage>>? = null

    private var siteTermsListSubmitObserver:  ListSubmitObserver<SiteTermsWithLanguage>? = null

    private var repo: UmAppDatabase? = null


    override var siteTermsList: DataSource.Factory<Int, SiteTermsWithLanguage>? = null
        set(value) {
            val siteTermsDao = repo?.siteTermsDao ?: return
            siteTermsListSubmitObserver?.run {
                currentSiteTermsLiveData?.removeObserver(this)
            }

            if(view == null)
                return //check in case of lifecycle issue

            currentSiteTermsLiveData = value?.asRepositoryLiveData(siteTermsDao)
            siteTermsListSubmitObserver?.run {
                currentSiteTermsLiveData?.observe(viewLifecycleOwner, this)
            }

            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentSiteDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

        siteRecyclerViewAdapter = SiteRecyclerViewAdapter()
        mSiteTermsRecyclerViewAdapter = SiteTermsRecyclerViewAdapter().also {
            siteTermsListSubmitObserver = ListSubmitObserver(it)
        }

        val termsHeaderRecyclerViewAdapter = SimpleHeadingRecyclerAdapter(
                requireContext().getString(R.string.terms_and_policies)).also  {
                    it.visible = true
        }
        mMergeAdapter = ConcatAdapter(siteRecyclerViewAdapter, termsHeaderRecyclerViewAdapter,
                mSiteTermsRecyclerViewAdapter)

        mBinding?.fragmentListRecyclerview?.apply {
            adapter = mMergeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentListRecyclerview?.adapter = null
        mBinding = null
        mPresenter = null
        entity = null
    }


    companion object {
        val DIFFUTIL_SITE = object: DiffUtil.ItemCallback<Site>() {
            override fun areItemsTheSame(oldItem: Site, newItem: Site): Boolean {
                return oldItem.siteUid == newItem.siteUid
            }

            override fun areContentsTheSame(oldItem: Site, newItem: Site): Boolean {
                return oldItem.siteName == newItem.siteName &&
                        oldItem.registrationAllowed == newItem.registrationAllowed &&
                        oldItem.guestLogin == newItem.guestLogin
            }
        }

        val DIFFUTIL_SITE_TERMS = object: DiffUtil.ItemCallback<SiteTermsWithLanguage>() {
            override fun areItemsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsUid == newItem.sTermsUid
            }

            override fun areContentsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsLangUid == newItem.sTermsLangUid
            }
        }

    }

}

@Composable
fun SiteDetailScreen(
    uiState: SiteDetailUiState,
    onClickLang: (SiteTermsWithLanguage) -> Unit = {}
){

    Column {
        UstadDetailField(
            valueText = uiState.site?.siteName ?: "",
            labelText = stringResource(R.string.name),
            imageId = R.drawable.ic_account_balance_black_24dp
        )
        UstadDetailField(valueText = stringResource(if(uiState.site?.guestLogin == true){R.string.yes} else {R.string.no}), labelText = "Guest login enabled", imageId = R.drawable.ic_document_preview)
        UstadDetailField(valueText = stringResource(if(uiState.site?.registrationAllowed == true){R.string.yes} else {R.string.no}), labelText = "Registration allowed", imageId = R.drawable.ic_baseline_how_to_reg_24)
        Text(stringResource(R.string.terms_and_policies), style = Typography.h6)

        uiState.siteTerms.forEach {
            TextButton(
                onClick = { onClickLang(it) },
            ) {
                Text(text = it.stLanguage?.name ?: "")
            }
        }
    }
}

@Composable
@Preview
fun SiteDetailScreenPreview(){
    SiteDetailScreen(
        uiState = SiteDetailUiState(
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

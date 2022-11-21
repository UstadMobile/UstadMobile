package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteTermsDetailBinding
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.controller.SiteTermsDetailPresenter
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.core.viewmodel.ParentalConsentManagementUiState
import com.ustadmobile.core.viewmodel.SiteTermsDetailUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.binding.loadHtmlData
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField


class SiteTermsDetailFragment: UstadDetailFragment<SiteTerms>(), SiteTermsDetailView {

    private var mBinding: FragmentSiteTermsDetailBinding? = null

    private var mPresenter: SiteTermsDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var acceptButtonVisible: Boolean = false
        set(value) {
            field = value
            activity?.invalidateOptionsMenu()
        }

    override var entity: SiteTerms? = null
        get() = field
        set(value) {
            field = value
            mBinding?.workspaceTerms = value
            mBinding?.termsWebview?.loadData(value?.termsHtml ?: "", "text/html",
                    "UTF-8")
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.takeIf { acceptButtonVisible }?.inflate(R.menu.menu_accept, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteTermsDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteTermsDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.accept -> {
                mPresenter?.handleClickAccept()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }



}

@Composable
fun SiteTermsDetailScreen(
    uiState: SiteTermsDetailUiState = SiteTermsDetailUiState()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                webViewClient = WebViewClient()
                loadHtmlData(uiState.siteTerms?.termsHtml)
                setTag(R.id.tag_webview_html, uiState.siteTerms?.termsHtml)
            }},
            update = {
                if(uiState.siteTerms?.termsHtml != it.getTag(R.id.tag_webview_html))
                    it.loadUrl(uiState.siteTerms?.termsHtml!!)
            }
        )
    }
}

@Composable
@Preview
fun SiteTermsDetailScreenPreview() {
    val uiState = SiteTermsDetailUiState(
        siteTerms = SiteTerms().apply {
            termsHtml = "https://www.ustadmobile.com"
        },
    )

    MdcTheme {
        SiteTermsDetailScreen(uiState)
    }
}
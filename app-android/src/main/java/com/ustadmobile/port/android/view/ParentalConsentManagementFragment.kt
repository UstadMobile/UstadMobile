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
import com.google.accompanist.themeadapter.appcompat.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentParentalConsentManagementBinding
import com.ustadmobile.core.controller.ParentalConsentManagementPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.core.viewmodel.ParentalConsentManagementUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.view.binding.loadHtmlData
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadInputFieldLayout
import com.ustadmobile.port.android.view.util.ClearErrorTextWatcher


interface ParentAccountLandingFragmentEventHandler {
    fun onClickConsent()
    fun onClickDoNotConsent()
    fun onClickChangeConsent()
}

class ParentalConsentManagementFragment: UstadEditFragment<PersonParentJoinWithMinorPerson>(), ParentalConsentManagementView, ParentAccountLandingFragmentEventHandler {

    private var mBinding: FragmentParentalConsentManagementBinding? = null

    private var mPresenter: ParentalConsentManagementPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonParentJoinWithMinorPerson>?
        get() = mPresenter


    override var infoText: String?
        get() = mBinding?.infoText
        set(value) {
            mBinding?.infoText = value
        }

    override var siteTerms: SiteTerms?
        get() = mBinding?.siteTerms
        set(value) {
            mBinding?.siteTerms = value
        }

    override var relationshipFieldOptions: List<IdOption>?
        get() = mBinding?.relationshipFieldOptions
        set(value) {
            mBinding?.relationshipFieldOptions = value
        }

    override var relationshipFieldError: String?
        get() = mBinding?.relationshipFieldError
        set(value) {
            mBinding?.relationshipFieldError = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentParentalConsentManagementBinding.inflate(inflater, container, false).also { binding ->
            rootView = binding.root
            binding.eventHandler = this
            binding.relationshipValue.addTextChangedListener(ClearErrorTextWatcher {
                binding.relationshipFieldError = null
            })
        }

        mPresenter = ParentalConsentManagementPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //do nothing: this descends from edit fragment, but does not use the done checkbox
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onClickConsent() {
        entity?.also {
            it.ppjStatus = PersonParentJoin.STATUS_APPROVED
            mPresenter?.handleClickSave(it)
        }
    }

    override fun onClickDoNotConsent() {
        entity?.also {
            it.ppjStatus  = PersonParentJoin.STATUS_REJECTED
            mPresenter?.handleClickSave(it)
        }
    }

    override fun onClickChangeConsent() {
        entity?.also {
            it.ppjStatus = if(it.ppjStatus == PersonParentJoin.STATUS_APPROVED) {
                PersonParentJoin.STATUS_REJECTED
            }else {
                PersonParentJoin.STATUS_APPROVED
            }

            mPresenter?.handleClickSave(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.eventHandler = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: PersonParentJoinWithMinorPerson?
        get() = mBinding?.personParentJoin
        set(value) {
            mBinding?.personParentJoin = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}

@Composable
private fun ParentalConsentManagementScreen(
    uiState: ParentalConsentManagementUiState = ParentalConsentManagementUiState(),
    onClickConsent: () -> Unit = {},
    onClickDoNotConsent: () -> Unit = {},
    onClickChangeConsent: () -> Unit = {},
    onChangeRelation: (PersonParentJoin?) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        val minorDateOfBirth = rememberFormattedDate(
            timeInMillis = uiState.personParentJoin?.minorPerson?.dateOfBirth ?: 0,
            timeZoneId = UstadMobileConstants.UTC,
        )
        Text(stringResource(id = R.string.parent_consent_explanation,
            uiState.personParentJoin?.minorPerson?.fullName() ?: "",
            minorDateOfBirth, uiState.appName))

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.relationshipVisible){
            UstadInputFieldLayout(
                modifier = Modifier.fillMaxWidth(),
                errorText = uiState.relationshipError,
            ) {
                UstadMessageIdOptionExposedDropDownMenuField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.personParentJoin?.ppjRelationship ?: 0,
                    label = stringResource(R.string.relationship),
                    options = PersonParentJoinConstants.RELATIONSHIP_MESSAGE_IDS,
                    onOptionSelected = {
                        onChangeRelation(uiState.personParentJoin?.shallowCopy{
                            ppjRelationship = it.value
                        })
                    },
                    isError = uiState.relationshipError != null,
                    enabled = uiState.fieldsEnabled,
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(id = R.string.terms_and_policies),
            style = Typography.h4
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                if(uiState.siteTerms?.termsHtml != it.getTag(R.id.tag_webview_html)) {
                    it.loadHtmlData(uiState.siteTerms?.termsHtml)
                    it.setTag(R.id.tag_webview_html, uiState.siteTerms?.termsHtml)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.consentVisible){
            Button(
                onClick = onClickConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(R.string.i_consent).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor)
                    )
                )
            }
        }

        if (uiState.dontConsentVisible){
            OutlinedButton(
                onClick = onClickDoNotConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(stringResource(R.string.i_do_not_consent).uppercase())
            }
        }
        if (uiState.changeConsentVisible){
            val changeConsentText: Int =
                if (uiState.personParentJoin?.ppjStatus == PersonParentJoin.STATUS_APPROVED)
                    R.string.revoke_consent
                else
                    R.string.restore_consent

            Button(
                onClick = onClickChangeConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(changeConsentText).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor)
                    )
                )
            }
        }
    }
}

@Composable
@Preview
fun ParentalConsentManagementScreenPreview() {
    val uiState = ParentalConsentManagementUiState(
        siteTerms = SiteTerms().apply {
            termsHtml = "https://www.ustadmobile.com"
        },
        personParentJoin = PersonParentJoinWithMinorPerson().apply {
            ppjParentPersonUid = 0
            ppjRelationship = 1
            minorPerson = Person().apply {
                firstNames = "Pit"
                lastName = "The Young"
            }
        },
        fieldsEnabled = true
    )

    MdcTheme {
        ParentalConsentManagementScreen(uiState)
    }
}
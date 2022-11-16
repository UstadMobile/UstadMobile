package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteEnterLinkBinding
import com.ustadmobile.core.controller.SiteEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.core.viewmodel.SiteEnterLinkUiState
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import java.util.*


class SiteEnterLinkFragment : UstadBaseFragment(), SiteEnterLinkView{

    private var mBinding: FragmentSiteEnterLinkBinding? = null

    private var mPresenter: SiteEnterLinkPresenter? = null

    private val inputCheckDelay: Long = 500

    private val inputCheckHandler: Handler = Handler(Looper.getMainLooper())

    private val inputCheckerCallback = Runnable {
        val typedLink = siteLink
        if(typedLink != null){
            progressVisible = true
            mPresenter?.handleCheckLinkText(typedLink)
        }
    }

    override var siteLink: String?
        get() = mBinding?.siteLink
        set(value) {}

    override var validLink: Boolean = false
        set(value) {
            handleError(!value)
            mBinding?.showButton = value
            field = value
        }

    override var progressVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.showProgress = value
        }

    private fun handleError(isError: Boolean){
        mBinding?.siteLinkView?.isErrorEnabled = isError
        mBinding?.siteLinkView?.error = if(isError) getString(R.string.invalid_link) else null
    }

    private val uiState = SiteEnterLinkUiState()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = FragmentSiteEnterLinkBinding.inflate(inflater, container, false).also {
            it.showButton = false
            it.showProgress = false
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    SiteEnterLinkScreen(uiState)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteEnterLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
            this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        mBinding?.presenter = mPresenter
        mBinding?.organisationLink?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(url: CharSequence?, start: Int, before: Int, count: Int) {
                inputCheckHandler.removeCallbacks(inputCheckerCallback)
            }
            override fun afterTextChanged(s: Editable?) {
                inputCheckHandler.postDelayed(inputCheckerCallback, inputCheckDelay)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        siteLink = null
        mBinding = null
    }
}

@Composable
private fun SiteEnterLinkScreen(
    uiState: SiteEnterLinkUiState = SiteEnterLinkUiState(),
    onClickNext: () -> Unit = {},
    onClickNewLearningEnvironment: () -> Unit = {},
    onEditTextValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Image(
            painter = painterResource(id = R.drawable.illustration_connect),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp))

        Text(stringResource(R.string.please_enter_the_linK))

        UstadTextEditField(
            value = uiState.siteLink,
            label = stringResource(id = R.string.site_link),
            onValueChange = onEditTextValueChange,
            error = uiState.linkError,
            enabled = uiState.fieldsEnabled,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClickNext,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(stringResource(R.string.next).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.or).uppercase())

        Button(
            onClick = onClickNewLearningEnvironment,
            modifier = Modifier
                .fillMaxWidth(),
            elevation = null,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = colorResource(id = R.color.primaryColor),
            )
        ) {

            Icon(Icons.Filled.Add,
                contentDescription = "",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )

            Spacer(Modifier.size(ButtonDefaults.IconSpacing))

            Text(stringResource(R.string.create_a_new_learning_env)
                .uppercase()
            )
        }
    }
}

@Composable
@Preview
fun SiteEnterLinkScreenPreview() {
    MdcTheme {
        SiteEnterLinkScreen()
    }
}


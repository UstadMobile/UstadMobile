package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteEnterLinkBinding
import com.ustadmobile.core.controller.SiteEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.core.viewmodel.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDateEditTextField
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteEnterLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.showButton = false
            it.showProgress = false
        }

        return rootView
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

        Text(
            stringResource(R.string.please_enter_the_linK),
            style = Typography.h4)

        UstadTextEditField(
            value = "",
            label = stringResource(id = R.string.site_link),
            onValueChange = onEditTextValueChange,
            error =null,
            enabled = true,
        )

        Spacer(modifier = Modifier.height(20.dp))

        NextButton(onClickNext)

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.or))

        NewLearningEnvironmentButton(onClickNewLearningEnvironment)
    }
}

@Composable
private fun NextButton(
    onClickNext: () -> Unit = {}
){
    TextButton(
        onClick = onClickNext,
        modifier = Modifier
            .background(colorResource(id = R.color.secondaryColor))
            .fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.next),
            style = Typography.h3,
            color = colorResource(id = R.color.almost_black))
    }
}

@Composable
private fun NewLearningEnvironmentButton(
    onClickNewLearningEnvironment: () -> Unit = {}
){
    TextButton(
        onClick = onClickNewLearningEnvironment,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_add_black_24dp),
            contentDescription = null,
            colorFilter = ColorFilter
                .tint(color = colorResource(id = R.color.primaryColor)),)

        Text(
            stringResource(R.string.create_a_new_learning_env),
            style = Typography.h3,
            color = colorResource(id = R.color.primaryColor))
    }
}

@Composable
@Preview
fun SiteEnterLinkScreenPreview() {
    SiteEnterLinkScreen()
}


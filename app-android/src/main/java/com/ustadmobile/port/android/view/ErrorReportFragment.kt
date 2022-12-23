package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentErrorReportBinding
import com.ustadmobile.core.controller.ErrorReportPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.core.viewmodel.ErrorReportUiState
import com.ustadmobile.lib.db.entities.ErrorReport
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import kotlin.text.Typography

interface ErrorReportFragmentEventHandler {

    fun onClickCopyIncidentId(id: Long)

    fun onClickShareIncidentId(id: Long)

}

class ErrorReportFragment : UstadBaseFragment(), ErrorReportFragmentEventHandler, ErrorReportView {

    private var mBinding: FragmentErrorReportBinding? = null

    private var mPresenter: ErrorReportPresenter? = null

    override var errorReport: ErrorReport?
        get() = mBinding?.errorReport
        set(value) {
            mBinding?.errorReport = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentErrorReportBinding.inflate(inflater, container, false).also {
            it.eventHandler = this
        }

        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = ErrorReportPresenter(requireContext(),
                arguments.toStringMap(), this, di).withViewLifecycle()

        mBinding?.mPresenter = mPresenter
        mPresenter?.onCreate(savedInstanceState?.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding?.mPresenter = null
        mBinding?.eventHandler = null
        mBinding = null
    }

    override fun onClickCopyIncidentId(id: Long) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(
            requireContext().getString(R.string.incident_id), id.toString() ?: "-1"))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    override fun onClickShareIncidentId(id: Long) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, id.toString())
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }
}

@Composable
fun ErrorReportScreen(
    uiState: ErrorReportUiState,
    onTakeMeHomeClick: () -> Unit = {},
    onCopyIconClick: () -> Unit = {},
    onShareIconClick: () -> Unit = {}
){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
        ){

        Image(
            modifier = Modifier
                .padding(vertical = 16.dp),
            painter = painterResource(id = R.drawable.ic_undraw_access_denied),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.sorry_something_went_wrong)
        )

        Button(
            onClick = onTakeMeHomeClick,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = stringResource(R.string.take_me_home).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }

        Divider(thickness = 1.dp)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = uiState.errorReport?.errUid.toString(),
                    style = Typography.body1
                )
                Text(
                    text = stringResource(id = R.string.incident_id),
                    style = Typography.body2
                )
            }

            Row {
                IconButton(
                    onClick = {
                        onCopyIconClick()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_file_copy_24),
                        contentDescription = stringResource(R.string.copy_code)
                    )
                }
                IconButton(
                    onClick = {
                        onShareIconClick()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = stringResource(R.string.share)
                    )
                }
            }
        }

        Divider(thickness = 1.dp)

        Text(
            text = stringResource(id = R.string.error_code,
                uiState.errorReport?.errorCode ?: ""),
            style = Typography.body1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = uiState.errorReport?.message ?: "",
            style = Typography.body2
        )

    }
}

@Composable
@Preview
fun ErrorReportPreview(){
    ErrorReportScreen(
        uiState = ErrorReportUiState(
            errorReport = ErrorReport().apply {
                errorCode = 1234
                errUid = 1234123112
                message = "6x7 is the question when you think about it"
            }
        )
    )
}
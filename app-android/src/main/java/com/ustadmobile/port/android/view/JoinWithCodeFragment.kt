package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentJoinWithCodeBinding
import com.ustadmobile.core.controller.JoinWithCodePresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.JoinWithCodeUiState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.core.R as CR

class JoinWithCodeFragment: UstadBaseFragment(), JoinWithCodeView {

    override var controlsEnabled: Boolean? = null
        get() = field
        set(value) {
            field = value
        }

    override var errorText: String? = null
        get() = field
        set(value) {
            mBinding?.errorText = value
            field = value
        }
    override var code: String? = null
        set(value) {
            mBinding?.joinCode = value
            field = value
        }

    private var mBinding: FragmentJoinWithCodeBinding? = null

    private var mPresenter: JoinWithCodePresenter? = null

    override var buttonLabel: String?
        get() = mBinding?.buttonLabel
        set(value) {
            mBinding?.buttonLabel = value
        }

    override var loading: Boolean
        get() = super.loading
        set(value) {
            super.loading = value
            mBinding?.buttonEnabled = !value
        }

    override fun finish() {
        findNavController().navigateUp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentJoinWithCodeBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableId = arguments?.get(UstadView.ARG_CODE_TABLE).toString().toInt()
        ustadFragmentTitle = if(tableId == Clazz.TABLE_ID){
            mBinding?.entityType = requireContext().getString(CR.string.clazz)
            requireContext().getString(CR.string.join_existing_class)
        }else if (tableId == School.TABLE_ID){
            mBinding?.entityType = requireContext().getString(CR.string.school)
            requireContext().getString(CR.string.join_existing_school)
        }else{
            mBinding?.entityType = ""
            "ERR - Unknown entity type"
        }

        mPresenter = JoinWithCodePresenter(requireContext(), arguments.toStringMap(), this,
            di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mBinding = null
    }
}

@Composable
fun JoinWithCodeScreen(
    uiState: JoinWithCodeUiState,
    onCodeValueChange: (String) -> Unit = {},
    onClickDone: () -> Unit = {},
){
    Column (
        modifier = Modifier
            .defaultScreenPadding()
            .fillMaxSize(),
    ){

       Text(
           stringResource(id = CR.string.join_code_instructions),
           modifier = Modifier.defaultItemPadding()
       )

        UstadTextEditField(
            modifier = Modifier.defaultItemPadding(),
            value = uiState.code,
            label = stringResource(id = CR.string.entity_code, uiState.entityType),
            error = uiState.codeError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCodeValueChange(it)
            },
        )

        Button(
            onClick = onClickDone,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            enabled = uiState.fieldsEnabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(uiState.buttonLabel.uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }
    }
}

@Composable
@Preview
fun JoinWithCodeScreenPreview(){
    JoinWithCodeScreen(
        uiState = JoinWithCodeUiState(
            entityType = "Course",
            buttonLabel = "Join course"
        )
    )
}
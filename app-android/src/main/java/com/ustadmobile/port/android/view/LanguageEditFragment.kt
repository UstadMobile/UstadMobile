package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLanguageEditBinding
import com.ustadmobile.core.controller.LanguageEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.core.viewmodel.LanguageEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class LanguageEditFragment: UstadEditFragment<Language>(), LanguageEditView {

    private var mBinding: FragmentLanguageEditBinding? = null

    private var mPresenter: LanguageEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Language>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentLanguageEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = LanguageEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_new_language, R.string.edit_language)


        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: Language? = null
        get() = field
        set(value) {
            field = value
            mBinding?.language = value
        }

    override var langNameError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.langNameError = value
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
fun LanguageEditScreen(
    uiState: LanguageEditUiState,
    onLanguageChanged: (Language?) -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.Start
    ){

        UstadTextEditField(
            value = uiState.language?.name ?: "",
            label = stringResource(id = R.string.name),
            error = uiState.languageNameError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onLanguageChanged(uiState.language?.shallowCopy{
                    name = it
                })
            }
        )

        UstadTextEditField(
            value = uiState.language?.iso_639_1_standard ?: "",
            label = stringResource(id = R.string.two_letter_code),
            error = uiState.twoLettersCodeError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onLanguageChanged(uiState.language?.shallowCopy{
                    iso_639_1_standard = it
                })
            }
        )

        UstadTextEditField(
            value = uiState.language?.iso_639_2_standard ?: "",
            label = stringResource(id = R.string.three_letter_code),
            error = uiState.threeLettersCodeError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onLanguageChanged(uiState.language?.shallowCopy{
                    iso_639_2_standard = it
                })
            }
        )
    }
}

@Composable
@Preview
fun LanguageEditScreenPreview(){
    LanguageEditScreen(
        uiState = LanguageEditUiState(
            language = Language().apply {
                name = "fa"
            }
        )
    )
}
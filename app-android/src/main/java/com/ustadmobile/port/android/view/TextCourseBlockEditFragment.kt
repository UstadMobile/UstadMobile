package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentTextCourseBlockEditBinding
import com.ustadmobile.core.controller.TextCourseBlockEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.TextCourseBlockEditView
import com.ustadmobile.core.viewmodel.TextCourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadDateEditTextField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.port.android.view.composable.UstadTimeEditTextField
import com.ustadmobile.port.android.view.composable.addOptionalSuffix
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import java.util.*

class TextCourseBlockEditFragment: UstadEditFragment<CourseBlock>(), TextCourseBlockEditView,
    IAztecToolbarClickListener {

    private var mBinding: FragmentTextCourseBlockEditBinding? = null

    private var mPresenter: TextCourseBlockEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlock>?
        get() = mPresenter

    private var aztec: Aztec? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentTextCourseBlockEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            aztec = Aztec.with(it.editor,  it.formattingToolbar, this).also {
                it.visualEditor.setCalypsoMode(false)
                it.addPlugin(CssUnderlinePlugin())
                it.initSourceEditorHistory()
            }
        }

        mPresenter = TextCourseBlockEditPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_text, R.string.edit_text)
    }

    override fun onSaveStateToBackStackStateHandle() {
        mBinding?.block?.cbDescription = aztec?.visualEditor?.toHtml()
        super.onSaveStateToBackStackStateHandle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done) {
            mBinding?.block?.cbDescription = aztec?.visualEditor?.toHtml()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        aztec = null
    }

    override var loading: Boolean = false

    override var entity: CourseBlock? = null
        get() = field
        set(value) {
            field = value
            mBinding?.block = value
            val description = value?.cbDescription
            if(description != null)
                aztec?.visualEditor?.fromHtml(description)

        }
    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }

    override fun onToolbarCollapseButtonClicked() {

    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }


}

@Composable
private fun TextCourseBlockEditScreen(
    uiState: TextCourseBlockEditUiState = TextCourseBlockEditUiState(),
    onBlockChanged: (CourseBlock?) -> Unit = {},
    onStartDateChanged: (Long?) -> Unit = {},
    onStartTimeChanged: (Int?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
    )  {

        UstadTextEditField(
            value = uiState.block?.cbTitle ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.blockTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onBlockChanged(uiState.block?.shallowCopy{
                    cbTitle = it
                })
            },
        )

        Row (
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.startDate,
                label = stringResource(id = R.string.dont_show_before).addOptionalSuffix(),
                enabled = uiState.fieldsEnabled,
                timeZoneId = TimeZone.getDefault().id,
                onValueChange = {
                    onStartDateChanged(it)
                },
            )

            UstadTimeEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.startTime,
                label = stringResource(id = R.string.time),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onStartTimeChanged(it)
                },
            )
        }

    }
}

@Composable
@Preview
fun TextCourseBlockEditScreenPreview() {
    MdcTheme {
        TextCourseBlockEditScreen()
    }
}
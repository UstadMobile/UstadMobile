package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentErrorReportBinding
import com.ustadmobile.core.controller.ErrorReportPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.lib.db.entities.ErrorReport

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
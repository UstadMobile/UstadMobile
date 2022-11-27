package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.pdfview.PDFView
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView
import com.toughra.ustadmobile.databinding.FragmentPdfContentBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PDFContentPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PDFContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class PDFContentFragment : UstadBaseFragment(), PDFContentView {

    private var mBinding: FragmentPdfContentBinding? = null

    private var mPresenter: PDFContentPresenter? = null

    private var pdfView: PDFView? = null

    private var rootView: View? = null

    private var db: UmAppDatabase? = null

    private var containerUid: Long = 0

    private val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = FragmentPdfContentBinding.inflate(inflater, container, false).also {
            rootView = it.root
            pdfView = it.fragmentPdfContentPdfview
        }
        pdfView?.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)

        pdfView?.setOnImageEventListener(object: SubsamplingScaleImageView.OnImageEventListener {
            override fun onReady() {
            }

            override fun onImageLoaded() {
            }

            override fun onPreviewLoadError(e: java.lang.Exception?) {
                showOpenError(e?.message?:"")
            }

            override fun onImageLoadError(e: java.lang.Exception?) {
                showOpenError(e?.message?:"")
            }

            override fun onTileLoadError(e: java.lang.Exception?) {
            }

            override fun onPreviewReleased() {
            }

        })

        val accountManager: UstadAccountManager = di.direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        containerUid = arguments?.getString(UstadView.ARG_CONTAINER_UID)?.toLong() ?: 0L


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = PDFContentPresenter(requireContext(),
            arguments.toStringMap(), this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        pdfView = null
        rootView = null
    }

    override var entry: ContentEntry? = null
        set(value) {
            field = value
            ustadFragmentTitle = value?.title
            mBinding?.entry = value
        }

    override var pdfContainerUid: Long = 0
        set(value) {
            field = value
            loading = false
            try{
                //Load PDF file from database:
                viewLifecycleOwner.lifecycleScope.launch {
                    val filePath: String? =
                        db?.containerEntryDao?.findByContainerAsync(containerUid)?.get(0)?.containerEntryFile?.cefPath
                    if(filePath != null){
                        pdfView?.fromFile(filePath)
                        pdfView?.show()
                    }else{
                        showError()
                    }
                }

            }catch(e: Exception){
                showError()
            }

        }

    private fun showError() {
        showSnackBar(systemImpl.getString(MessageID.error_opening_file,
                requireContext()), {}, 0)
    }

    fun showOpenError(message: String?) {
        showSnackBar(systemImpl.getString(MessageID.error_opening_file,
                requireContext()) + " " + message, {}, 0)
    }


}
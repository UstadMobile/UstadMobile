package com.ustadmobile.port.android.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import java.io.File

interface PersonEditFragmentEventHandler {

}

class PersonEditFragment: UstadEditFragment<Person>(), PersonEditView, PersonEditFragmentEventHandler {

    private var mBinding: FragmentPersonEditBinding? = null

    private var mPresenter: PersonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Person>?
        get() = mPresenter

    override var genderOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }

    private var dbRepo: UmAppDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null

    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.person)
    }

    override var entity: Person? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value

            //for some reason setting the options before (and indepently from) the value causes
            // a databinding problem
            mBinding?.genderOptions = genderOptions
        }


    /**
     * This may lead to I/O activity - do not call from the main thread!
     */
    override var personPicturePath: String?
        get() {
            val boundPicUri = mBinding?.personPictureUri
            if(boundPicUri == null) {
                return null
            }else{
                val uriObj = Uri.parse(boundPicUri)
                if(uriObj.scheme == "file") {
                    return uriObj.toFile().absolutePath
                }else {
                    val tmpFile = findNavController().createTempFileForDestination(requireContext(),
                            "personPicture-${System.currentTimeMillis()}")
                    try {
                        val input = (context as Context).contentResolver.openInputStream(uriObj) ?: return null
                        val output = tmpFile.outputStream()
                        input.copyTo(tmpFile.outputStream())
                        output.flush()
                        output.close()
                        input.close()
                        return tmpFile.absolutePath
                    }catch(e: Exception) {
                        e.printStackTrace()
                    }

                    return null
                }
            }
        }

        set(value) {
            if(value != null) {
                mBinding?.personPictureUri = Uri.fromFile(File(value)).toString()
            }else {
                mBinding?.personPictureUri = null
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

}
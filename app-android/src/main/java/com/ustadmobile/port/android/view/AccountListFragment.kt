package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentAccountListBinding
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEnterLinkBinding

class AccountListFragment : UstadBaseFragment() {

    private var mBinding: FragmentAccountListBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView: View
        mBinding = FragmentAccountListBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        return rootView
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}
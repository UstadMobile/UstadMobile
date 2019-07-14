package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable

import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.ComingSoonView

class ComingSoonFragment : UstadBaseFragment(), ComingSoonView {
    override val viewContext: Any
        get() = context!!

    internal var rootContainer: View ?= null

    override fun finish() {
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_coming_soon, container, false)
        setHasOptionsMenu(true)

        return rootContainer

    }

    companion object {

        fun newInstance(): ComingSoonFragment {
            val fragment = ComingSoonFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}

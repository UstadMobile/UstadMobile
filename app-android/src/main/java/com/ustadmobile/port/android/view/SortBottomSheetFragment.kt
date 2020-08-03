package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.util.SortOrderOption

class SortBottomSheetFragment(val sortOptions: List<SortOrderOption>?, var onSortOptionSelected: OnSortOptionSelected?) : BottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sort_option_list, container, false)
        // TODO create the list of options from sortOptions
        return rootView
    }

    //TODO: this goes into the view XML
    override fun onClick(v: View?) {
        val isShowing = this.dialog?.isShowing


        onSortOptionSelected?.onClickSort(SortOrderOption())

    }

    override fun onDestroyView() {
        super.onDestroyView()

        onSortOptionSelected = null
    }
}
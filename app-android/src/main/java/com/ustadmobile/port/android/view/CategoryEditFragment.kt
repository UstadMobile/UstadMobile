package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCategoryEditBinding
import com.ustadmobile.core.controller.CategoryEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CategoryEditView
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class CategoryEditFragment: UstadEditFragment<Category>(), CategoryEditView{

    private var mBinding: FragmentCategoryEditBinding? = null

    private var mPresenter: CategoryEditPresenter? = null


    override val mEditPresenter: UstadEditPresenter<*, Category>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCategoryEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }


        mPresenter = CategoryEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()   
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_category, R.string.edit_category)
    }

    override var entity: Category? = null
        get() = field
        set(value) {
            mBinding?.category = value
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

}
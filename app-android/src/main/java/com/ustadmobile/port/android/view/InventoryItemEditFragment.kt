package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentInventoryItemEditBinding
import com.ustadmobile.core.controller.InventoryItemEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.InventoryItemEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToEditEntity


interface InventoryItemEditFragmentEventHandler {

    fun addWE()
}

class InventoryItemEditFragment: UstadEditFragment<InventoryItem>(), InventoryItemEditView,
        InventoryItemEditFragmentEventHandler {

    private var mBinding: FragmentInventoryItemEditBinding? = null

    private var mPresenter: InventoryItemEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, InventoryItem>?
        get() = mPresenter

    private var producersRecyclerAdapter: PersonWithInventoryListRecyclerAdapter? = null
    private var producersListRecyclerView: RecyclerView? = null
    private val producersObserver = Observer<List<PersonWithInventory>?>{
        t ->
        run {
            producersRecyclerAdapter?.submitList(t)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentInventoryItemEditBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        producersRecyclerAdapter = PersonWithInventoryListRecyclerAdapter()
        producersListRecyclerView = rootView.findViewById(R.id.fragment_inventory_item_edit_edit_producers_rv)
        producersListRecyclerView?.adapter = producersRecyclerAdapter
        producersListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = InventoryItemEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                PersonWithInventory::class.java) {
            val producer = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditPersonWithInventory(producer)
        }

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        producersListRecyclerView?.adapter = null
        producersListRecyclerView = null
        producersRecyclerAdapter = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.receive_inventory, R.string.receive_inventory)
    }

    override var entity: InventoryItem? = null
        get() = field
        set(value) {
            field = value
            mBinding?.inventoryItem = value
        }

    override var producers: DoorMutableLiveData<List<PersonWithInventory>>? = null
        set(value) {
            field?.removeObserver(producersObserver)
            field = value
            value?.observe(this, producersObserver)
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }



    override fun addWE() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(null, R.id.person_edit_dest, Person::class.java
            ,argBundle = bundleOf(UstadView.ARG_FILTER_PERSON_WE to "true")
        )

    }
}
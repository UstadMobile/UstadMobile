package com.ustadmobile.port.android.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import java.io.File

interface PersonEditFragmentEventHandler {

    fun onClickNewClazzMemberWithClazz()
    
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
    
    private data class ClassRoleOption(val roleId: Int, val resultKey: String, val stringId: Int)

    class ClazzMemberWithClazzRecyclerAdapter(val eventHandler: PersonEditFragmentEventHandler,
        var presenter: PersonEditPresenter?): ListAdapter<ClazzMemberWithClazz, ClazzMemberWithClazzRecyclerAdapter.ClazzMemberWithClazzViewHolder>(DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ) {

        class ClazzMemberWithClazzViewHolder(val binding: ItemClazzMemberWithClazzEditBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzMemberWithClazzViewHolder {
            val viewHolder = ClazzMemberWithClazzViewHolder(ItemClazzMemberWithClazzEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mFragment = eventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzMemberWithClazzViewHolder, position: Int) {
            holder.binding.clazzMemberWithClazz = getItem(position)
        }
    }

    override var clazzList: DoorLiveData<List<ClazzMemberWithClazz>>? = null
        get() = field
        set(value) {
            field?.removeObserver(clazzMemberWithClazzObserver)
            field = value
            value?.observe(this, clazzMemberWithClazzObserver)
        } 
    
    private var clazzMemberWithClazzRecyclerAdapter: ClazzMemberWithClazzRecyclerAdapter? = null

    private var clazzMemberNewItemRecyclerViewAdapter: NewItemRecyclerViewAdapter? = null
    
    private val clazzMemberWithClazzObserver = Observer<List<ClazzMemberWithClazz>?> {
        t -> clazzMemberWithClazzRecyclerAdapter?.submitList(t)
    }

    override fun onClickNewClazzMemberWithClazz()  {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.role)
                .setItems(CLAZZ_ROLE_KEY_MAP.map {
                        requireContext().getString(it.stringId)
                }.toTypedArray()) { dialog, which ->
                    onSaveStateToBackStackStateHandle()
                    navigateToPickEntityFromList(Person::class.java, R.id.clazz_list_dest, bundleOf(),
                            CLAZZ_ROLE_KEY_MAP[which].resultKey, overwriteDestination = true)
                }.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.clazzlistRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }

        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        clazzMemberWithClazzRecyclerAdapter = ClazzMemberWithClazzRecyclerAdapter(this, mPresenter)
        clazzMemberNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(
                View.OnClickListener { onClickNewClazzMemberWithClazz() },
                requireContext().getString(R.string.add_person_to_class)).apply {
            newItemVisible = true
        }
        mBinding?.clazzlistRecyclerview?.adapter = MergeAdapter(clazzMemberWithClazzRecyclerAdapter,
                clazzMemberNewItemRecyclerViewAdapter)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.person)

        CLAZZ_ROLE_KEY_MAP.forEach {roleOption ->
            findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                    Clazz::class.java, roleOption.resultKey) {
                val clazzSelected = it.firstOrNull() ?: return@observeResult
                mPresenter?.handleAddOrEditClazzMemberWithClazz(ClazzMemberWithClazz().apply {
                    clazzMemberPersonUid = arguments?.getString(UstadView.ARG_ENTITY_UID)?.toLong() ?: 0L
                    clazzMemberClazzUid =  clazzSelected.clazzUid
                    clazzMemberRole = roleOption.roleId
                    clazz = clazzSelected
                    clazzMemberActive = true
                })
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null

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

    companion object {

        private val CLAZZ_ROLE_KEY_MAP = listOf(
                ClassRoleOption(ClazzMember.ROLE_STUDENT, "Person_Student", R.string.student),
                ClassRoleOption(ClazzMember.ROLE_TEACHER, "Person_Teacher", R.string.teacher))

        val DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ = object: DiffUtil.ItemCallback<ClazzMemberWithClazz>() {
            override fun areItemsTheSame(oldItem: ClazzMemberWithClazz, newItem: ClazzMemberWithClazz): Boolean {
                return oldItem.clazzMemberUid == newItem.clazzMemberUid
            }

            override fun areContentsTheSame(oldItem: ClazzMemberWithClazz, newItem: ClazzMemberWithClazz): Boolean {
                return oldItem == newItem
            }
        }
    }
}
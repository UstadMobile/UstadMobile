package com.ustadmobile.port.android.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File

interface PersonEditFragmentEventHandler {

    fun onClickNewClazzMemberWithClazz()

    fun onClickNewRoleAndAssignment()
    
}

class PersonEditFragment: UstadEditFragment<PersonWithAccount>(), PersonEditView,
        PersonEditFragmentEventHandler, EntityRoleItemHandler {

    private var mBinding: FragmentPersonEditBinding? = null

    private var mPresenter: PersonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    override var genderOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }

    private data class ClassRoleOption(val roleId: Int, val resultKey: String, val stringId: Int)

    class ClazzMemberWithClazzRecyclerAdapter(val eventHandler: PersonEditFragmentEventHandler,
        var presenter: PersonEditPresenter?): ListAdapter<ClazzMemberWithClazz,
            ClazzMemberWithClazzRecyclerAdapter.ClazzMemberWithClazzViewHolder>(
            DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ) {

        class ClazzMemberWithClazzViewHolder(val binding: ItemClazzMemberWithClazzEditBinding)
            : RecyclerView.ViewHolder(binding.root)

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

    override var rolesAndPermissionsList: DoorLiveData<List<EntityRoleWithNameAndRole>>? = null
        set(value) {
            field?.removeObserver(rolesAndPermissionObserver)
            field = value
            value?.observe(this, rolesAndPermissionObserver)
        }

    private var clazzMemberWithClazzRecyclerAdapter: ClazzMemberWithClazzRecyclerAdapter? = null

    private var clazzMemberUstadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var rolesAndPermissionUstadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null
    
    private val clazzMemberWithClazzObserver = Observer<List<ClazzMemberWithClazz>?> {
        t -> clazzMemberWithClazzRecyclerAdapter?.submitList(t)
    }

    private var rolesAndPermissionRecyclerAdapter: EntityRoleRecyclerAdapter? = null
    private val rolesAndPermissionObserver = Observer<List<EntityRoleWithNameAndRole>?> {
        t -> rolesAndPermissionRecyclerAdapter?.submitList(t)
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

    override fun onClickNewRoleAndAssignment() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(null, R.id.entityrole_edit_dest,
                EntityRoleWithNameAndRole::class.java, argBundle = bundleOf(
                    UstadView.ARG_FILTER_BY_PERSONGROUPUID to entity?.personGroupUid))

    }

    override fun handleClickEntityRole(entityRole: EntityRoleWithNameAndRole) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(entityRole, R.id.entityrole_edit_dest,
                EntityRoleWithNameAndRole::class.java)
    }


    override fun handleRemoveEntityRole(entityRole: EntityRoleWithNameAndRole) {
        mPresenter?.handleRemoveRoleAndPermission(entityRole)
    }

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value

            //for some reason setting the options before (and indepently from) the value causes
            // a databinding problem
            mBinding?.genderOptions = genderOptions
            loading = false
        }


    override var personPicture: PersonPicture?
        get() = mBinding?.personPicture
        set(value) {
            mBinding?.personPicture = value
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

    override var registrationMode: Boolean? = null
        set(value) {
            mBinding?.registrationMode = (value == true)
            field = value
        }

    override var usernameError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.usernameTextinputlayout, value != null, value)
        }

    override var firstNamesFieldError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.firstnamesTextinputlayout, value != null, value)
        }

    override var lastNameFieldError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.lastnameTextInputLayout, value != null, value)
        }

    override var genderFieldError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.genderTextinputlayout, value != null, value)
        }

    override var passwordError: String? = null
        set(value) {
            field = null
            handleInputError(mBinding?.passwordTextinputlayout, value != null, value)
        }


    override var noMatchPasswordError: String? = null
        set(value) {
            field = value
            if(value != null){
                handleInputError(mBinding?.passwordTextinputlayout,true, value)
                handleInputError(mBinding?.confirmPasswordTextinputlayout, true,value)
            }
        }

    override var confirmError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.confirmPasswordTextinputlayout, value != null, value)
        }

    override var dateOfBirthError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.birthdayTextinputlayout, value != null, value)
        }
    override var canDelegatePermissions: Boolean? = false
        set(value) {
            mBinding?.isAdmin = value?:false
            field = value
        }


    override fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
        val navController = findNavController()
        val destinationProvider: DestinationProvider = di.direct.instance()

        val umNextDestination = destinationProvider.lookupDestinationName(nextDestination)
        navController.currentBackStackEntry?.savedStateHandle?.set(UstadView.ARG_SNACK_MESSAGE,
                String.format(getString(R.string.logged_in_as),account?.username,account?.endpointUrl))
        if(umNextDestination != null){
            val navOptions = NavOptions.Builder().setPopUpTo(umNextDestination.destinationId, true).build()
            navController.navigate(umNextDestination.destinationId,null, navOptions)
        }
    }

    private fun handleInputError(inputView: TextInputLayout?, error: Boolean, hint: String?){
        inputView?.isErrorEnabled = error
        inputView?.error = if(error) hint else null
    }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.clazzlistRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.rolesAndPermissionsRv.layoutManager = LinearLayoutManager(requireContext())
            it.isAdmin = canDelegatePermissions?:false
        }

        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        clazzMemberWithClazzRecyclerAdapter = ClazzMemberWithClazzRecyclerAdapter(this, mPresenter)
        rolesAndPermissionRecyclerAdapter = EntityRoleRecyclerAdapter(true, this)
        clazzMemberUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                View.OnClickListener { onClickNewClazzMemberWithClazz() },
                requireContext().getString(R.string.add_person_to_class)).apply {
            newItemVisible = true
        }
        rolesAndPermissionUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                View.OnClickListener { onClickNewRoleAndAssignment() },
                requireContext().getString(R.string.add_role_permission)).apply {
            newItemVisible = true
        }
        mBinding?.clazzlistRecyclerview?.adapter = MergeAdapter(clazzMemberWithClazzRecyclerAdapter,
                clazzMemberUstadListHeaderRecyclerViewAdapter)

        mBinding?.rolesAndPermissionsRv?.adapter = MergeAdapter(rolesAndPermissionRecyclerAdapter,
                rolesAndPermissionUstadListHeaderRecyclerViewAdapter)

        mBinding?.usernameText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.usernameTextinputlayout, false, null)
            }
        })

        mBinding?.firstnamesText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.firstnamesTextinputlayout, false, null)
            }
        })

        mBinding?.lastnameText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.lastnameTextInputLayout, false, null)
            }
        })

        mBinding?.genderValue?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.genderTextinputlayout, false, null)
            }
        })


        mBinding?.passwordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.passwordTextinputlayout, false, null)
            }
        })

        mBinding?.confirmPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.confirmPasswordTextinputlayout, false, null)
            }
        })

        mBinding?.birthdayText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.birthdayTextinputlayout, false, null)
            }
        })


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)

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

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                EntityRoleWithNameAndRole::class.java ) {
            val entityRole = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditRoleAndPermission(entityRole)
        }

        if(registrationMode == true) {
            ustadFragmentTitle = requireContext().getString(R.string.register)
        }else {
            setEditFragmentTitle(R.string.add_a_new_person, R.string.edit_person)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null

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
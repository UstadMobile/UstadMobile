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
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentWithClazzEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzList2View.Companion.ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST
import com.ustadmobile.core.view.ClazzMemberListView.Companion.ARG_HIDE_CLAZZES
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_PERMISSION
import com.ustadmobile.core.view.UstadView.Companion.ARG_GO_TO_COMPLETE
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.view.PersonAccountEditFragment.Companion.USERNAME_FILTER
import com.ustadmobile.port.android.view.binding.ImageViewLifecycleObserver2
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import kotlinx.coroutines.runBlocking
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

    class ClazzEnrolmentWithClazzRecyclerAdapter(val eventHandler: PersonEditFragmentEventHandler,
            var presenter: PersonEditPresenter?): ListAdapter<ClazzEnrolmentWithClazz,
            ClazzEnrolmentWithClazzRecyclerAdapter.ClazzEnrolmentWithClazzViewHolder>(
            DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ) {

        class ClazzEnrolmentWithClazzViewHolder(val binding: ItemClazzEnrolmentWithClazzEditBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentWithClazzViewHolder {
            val viewHolder = ClazzEnrolmentWithClazzViewHolder(ItemClazzEnrolmentWithClazzEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mFragment = eventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentWithClazzViewHolder, position: Int) {
            holder.binding.clazzEnrolmentWithClazz = getItem(position)
        }
    }


    override var clazzList: DoorLiveData<List<ClazzEnrolmentWithClazz>>? = null
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

    private var clazzEnrolmentWithClazzRecyclerAdapter: ClazzEnrolmentWithClazzRecyclerAdapter? = null

    private var clazzMemberUstadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var rolesAndPermissionUstadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null
    
    private val clazzMemberWithClazzObserver = Observer<List<ClazzEnrolmentWithClazz>?> {
        t -> clazzEnrolmentWithClazzRecyclerAdapter?.submitList(t)
    }

    private var rolesAndPermissionRecyclerAdapter: EntityRoleRecyclerAdapter? = null
    private val rolesAndPermissionObserver = Observer<List<EntityRoleWithNameAndRole>?> {
        t -> rolesAndPermissionRecyclerAdapter?.submitList(t)
    }

    override fun onClickNewClazzMemberWithClazz()  {
        onSaveStateToBackStackStateHandle()
        val listOfClazzSelected = clazzEnrolmentWithClazzRecyclerAdapter
                ?.currentList?.map { it.clazzEnrolmentClazzUid } ?: listOf()
        navigateToPickEntityFromList(ClazzEnrolmentWithClazz::class.java, R.id.clazz_list_dest,
                overwriteDestination = true, args = bundleOf(
                ARG_FILTER_BY_PERMISSION to Role.PERMISSION_CLAZZ_ADD_STUDENT.toString(),
                ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST to listOfClazzSelected.joinToString(),
                ARG_POPUPTO_ON_FINISH to PersonEditView.VIEW_NAME,
                ARG_GO_TO_COMPLETE to ClazzEnrolmentEditView.VIEW_NAME))
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

    override var usernameError: String?
        set(value) {
            mBinding?.usernameError = value
        }
        get() = mBinding?.usernameError

    override var emailError: String?
        set(value){
            mBinding?.emailError = value
        }
        get()= mBinding?.emailError

    override var firstNamesFieldError: String?
        set(value) {
            mBinding?.firstNamesError = value
        }
        get() = mBinding?.firstNamesError

    override var lastNameFieldError: String?
        set(value) {
            mBinding?.lastNameError = value
        }
        get() = mBinding?.lastNameError

    override var genderFieldError: String?
        set(value) {
            mBinding?.genderFieldError = value
        }
        get() = mBinding?.genderFieldError

    override var passwordError: String?
        set(value) {
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordError

    override var noMatchPasswordError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordConfirmError


    override var confirmError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordConfirmError

    override var dateOfBirthError: String?
        set(value) {
            mBinding?.dateOfBirthFieldError = value
        }
        get() = mBinding?.dateOfBirthFieldError

    override var lastNameError: String?
        set(value) {
            mBinding?.lastNameError = value
        }
        get() = mBinding?.lastNameError

    override var firstNameError: String?
        set(value) {
            mBinding?.firstNamesError = value
        }
        get() = mBinding?.firstNamesError

    override var canDelegatePermissions: Boolean? = false
        set(value) {
            mBinding?.isAdmin = value?:false
            field = value
        }

    private var imageViewLifecycleObserver: ImageViewLifecycleObserver2? = null

    override fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
        val navController = findNavController()
        val destinationProvider: DestinationProvider = di.direct.instance()

        val umNextDestination = destinationProvider.lookupDestinationName(nextDestination)
        navController.currentBackStackEntry?.savedStateHandle?.set(UstadView.ARG_SNACK_MESSAGE,
                String.format(getString(R.string.logged_in_as),account?.username,account?.endpointUrl))
        if(umNextDestination != null){
            val navOptions = NavOptions.Builder().setPopUpTo(umNextDestination.destinationId,
                    true).build()
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

    class ClearErrorTextWatcher(private val onTextFunction: () -> Unit ):TextWatcher{
        override fun afterTextChanged(p0: Editable?) {

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextFunction()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewLifecycleObserver = ImageViewLifecycleObserver2(
            requireActivity().activityResultRegistry,null, 1).also {
            lifecycle.addObserver(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.imageViewLifecycleObserver = imageViewLifecycleObserver
            it.clazzlistRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.rolesAndPermissionsRv.layoutManager = LinearLayoutManager(requireContext())
            it.isAdmin = canDelegatePermissions?:false
            it.hideClazzes =  arguments?.getString(ARG_HIDE_CLAZZES)?.toBoolean() ?: false
        }

        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        clazzEnrolmentWithClazzRecyclerAdapter =
                ClazzEnrolmentWithClazzRecyclerAdapter(this, mPresenter)
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
        mBinding?.clazzlistRecyclerview?.adapter = ConcatAdapter(clazzEnrolmentWithClazzRecyclerAdapter,
                clazzMemberUstadListHeaderRecyclerViewAdapter)

        mBinding?.rolesAndPermissionsRv?.adapter = ConcatAdapter(rolesAndPermissionRecyclerAdapter,
                rolesAndPermissionUstadListHeaderRecyclerViewAdapter)

        mBinding?.usernameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.usernameError = null
        })

        mBinding?.firstnamesText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.firstNamesError = null
        })

        mBinding?.lastnameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.lastNameError = null
        })

        mBinding?.genderValue?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.genderFieldError = null
        })

        mBinding?.passwordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordError = null
        })

        mBinding?.confirmPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordConfirmError = null
        })

        mBinding?.birthdayText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.dateOfBirthFieldError = null
        })
        mBinding?.emailText?.addTextChangedListener(ClearErrorTextWatcher{
            mBinding?.emailError = null
        })

        mBinding?.usernameText?.filters = arrayOf(USERNAME_FILTER)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                ClazzEnrolmentWithClazz::class.java){
            val clazzEnrolmentSelected = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditClazzMemberWithClazz(clazzEnrolmentSelected.apply {
                clazzEnrolmentPersonUid = arguments?.getString(UstadView.ARG_ENTITY_UID)?.toLong() ?: 0L
            })
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

        val DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ =
                object: DiffUtil.ItemCallback<ClazzEnrolmentWithClazz>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithClazz,
                                         newItem: ClazzEnrolmentWithClazz): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithClazz,
                                            newItem: ClazzEnrolmentWithClazz): Boolean {
                return oldItem == newItem
            }
        }
    }
}
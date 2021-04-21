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
import com.ustadmobile.port.android.view.binding.ImageViewLifecycleObserver2
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File

interface PersonEditFragmentEventHandler {

    fun onClickNewClazzMemberWithClazz()

    fun onClickNewRoleAndAssignment()

    fun onClickCountry()
    
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

    override var connectivityStatusOptions: List<MessageIdOption>? = null
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

    override fun onClickCountry() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Country::class.java, R.id.country_list_dest)
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
            mBinding?.connectivityStatusOptions = connectivityStatusOptions

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

    override var passwordError: String? = null
        set(value) {
            field = value
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
    override var canDelegatePermissions: Boolean = false
        get() = field
        set(value) {
            mBinding?.isAdmin = value
            field = value
        }

    override var viewConnectivityPermission: Boolean = false
        get() = field
        set(value) {
            mBinding?.viewConnectivity = value
            field = value
        }

    override var lastNameError: String? = null
        get() = field
        set(value) {
            field = value
            handleInputError(mBinding?.lastnameTextInputLayout, value != null, value)
        }
    override var countryError: String? = null
        get() = field
        set(value) {
            field = value
            handleInputError(mBinding?.countryTextinputlayout, value != null, value)
        }

    override var homeConnectivityStatusError: String? = null
        get() = field
        set(value) {
            field = value
            handleInputError(mBinding?.homeConnectivityStatusTextinputlayout, value != null, value)
        }

    override var mobileConnectivityStatusError: String? = null
        get() = field
        set(value) {
            field = value
            handleInputError(mBinding?.mobileConnectivityStatusTextinputlayout, value != null, value)
        }

    override var homeConnectivityStatus: PersonConnectivity? = null
        get() = mBinding?.homeConnectivity
        set(value) {
            field = value
            mBinding?.homeConnectivity = value
        }

    override var mobileConnectivityStatus: PersonConnectivity? = null
        get() = mBinding?.mobileConnectivity
        set(value) {
            field = value
            mBinding?.mobileConnectivity = value
        }

    override var firstNameError: String? = null
        get() = field
        set(value) {
            field = value
            handleInputError(mBinding?.firstnamesTextinputlayout, value != null, value)
        }

    private var imageViewLifecycleObserver: ImageViewLifecycleObserver2? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewLifecycleObserver = ImageViewLifecycleObserver2(
            requireActivity().activityResultRegistry,null, 1).also {
            lifecycle.addObserver(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.imageViewLifecycleObserver = imageViewLifecycleObserver
            it.clazzlistRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.rolesAndPermissionsRv.layoutManager = LinearLayoutManager(requireContext())
            it.isAdmin = canDelegatePermissions
            it.viewConnectivity = viewConnectivityPermission
            it.hideClazzes =  arguments?.getString(ARG_HIDE_CLAZZES)?.toBoolean() ?: false
            it.activityEventHandler = this
        }

        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        clazzEnrolmentWithClazzRecyclerAdapter = ClazzEnrolmentWithClazzRecyclerAdapter(this, mPresenter)
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

        mBinding?.usernameText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.usernameTextinputlayout, false, null)
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

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                Country::class.java){
            val country = it.firstOrNull() ?: return@observeResult
            entity?.personCountry = country.code
            entity = entity
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

        val DIFFUTIL_CLAZZMEMBER_WITH_CLAZZ = object: DiffUtil.ItemCallback<ClazzEnrolmentWithClazz>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithClazz, newItem: ClazzEnrolmentWithClazz): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithClazz, newItem: ClazzEnrolmentWithClazz): Boolean {
                return oldItem == newItem
            }
        }
    }
}
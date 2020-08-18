package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntityroleEditBinding
import com.ustadmobile.core.controller.EntityRoleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EntityRoleEditView
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class EntityRoleEditFragment() : UstadEditFragment<EntityRoleWithNameAndRole>(), EntityRoleEditView,
        EntityRoleEditHandler {

    private var mDataBinding: FragmentEntityroleEditBinding? = null

    private var mPresenter: EntityRoleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, EntityRoleWithNameAndRole>?
        get() = mPresenter


    override val viewContext: Any
        get() = requireContext()


    override var entity: EntityRoleWithNameAndRole? = null
        set(value) {
            mDataBinding?.entityRole = value
            field = value
        }


    override var fieldsEnabled: Boolean = true
        set(value) {
            field = value
            mDataBinding?.fieldsEnabled = value
        }


    //TODO: this
    override fun handleClickScope(entityRole: EntityRoleWithNameAndRole) {
        onSaveStateToBackStackStateHandle()
        //navigateToEditEntity(null, R.id.schedule_edit_dest, Schedule::class.java)
    }

    //TODO: this
    override fun handleClickRole(entityRole: EntityRoleWithNameAndRole) {
        onSaveStateToBackStackStateHandle()
        //navigateToEditEntity(null, R.id.schedule_edit_dest, Schedule::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        mDataBinding = FragmentEntityroleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
        }

        mPresenter = EntityRoleEditPresenter(requireContext(), arguments.toStringMap(),
                this@EntityRoleEditFragment,
                 di, viewLifecycleOwner)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.clazz)

        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Role::class.java) {
            val role = it.firstOrNull() ?: return@observeResult
            entity?.erRoleUid = role.roleUid
            entity?.entityRoleRole = role
            mDataBinding?.fragmentEntityroleEditRoleTiet?.setText(role.roleName)
            mDataBinding?.entityRole = entity
        }

        //TODO: Handle selection of Scope and scope object


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding = null
    }

    companion object {


    }
}
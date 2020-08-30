package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntityroleEditBinding
import com.ustadmobile.core.controller.EntityRoleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EntityRoleEditView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


class EntityRoleEditFragment() : UstadEditFragment<EntityRoleWithNameAndRole>(), EntityRoleEditView,
        EntityRoleEditHandler {

    private var mDataBinding: FragmentEntityroleEditBinding? = null

    private var mPresenter: EntityRoleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, EntityRoleWithNameAndRole>?
        get() = mPresenter

    override var loading: Boolean = false

    override val viewContext: Any
        get() = requireContext()


    override var entity: EntityRoleWithNameAndRole? = null
        set(value) {
            mDataBinding?.entityRole = value
            field = value
        }

    override var errorText: String? = null
        set(value) {
            mDataBinding?.errorText = value
            field = value
        }


    override var fieldsEnabled: Boolean = false
        set(value) {
            field = value
            mDataBinding?.fieldsEnabled = value
        }


    override fun handleClickScope(entityRole: EntityRoleWithNameAndRole) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.scope_by)
                .setItems(listOf(
                    requireContext().getString(R.string.school),
                    requireContext().getString(R.string.clazz),
                    requireContext().getString(R.string.person)
                ).toTypedArray()) { dialog, which ->
                    onSaveStateToBackStackStateHandle()
                    when (which) {
                        0 -> {
                            navigateToPickEntityFromList(School::class.java, R.id.schoollist_dest)
                        }
                        1 -> {
                            navigateToPickEntityFromList(Clazz::class.java, R.id.clazz_list_dest)
                        }
                        2 -> {
                            navigateToPickEntityFromList(Person::class.java, R.id.person_list_dest)
                        }
                    }
                }.show()
    }

    override fun handleClickRole(entityRole: EntityRoleWithNameAndRole) {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Role::class.java, R.id.role_list_dest)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        mDataBinding = FragmentEntityroleEditBinding.inflate(inflater, container,
                false).also {
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
        setEditFragmentTitle(R.string.assign_role)

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

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                School::class.java) {
            val school = it.firstOrNull() ?: return@observeResult
            entity?.erEntityUid = school.schoolUid
            entity?.entityRoleScopeName =
                    school.schoolName
            entity?.erTableId = School.TABLE_ID
            mDataBinding?.fragmentEntityroleEditScopeTiet?.setText(
                    school.schoolName )
            mDataBinding?.entityRole = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Clazz::class.java) {
            val clazz = it.firstOrNull() ?: return@observeResult
            entity?.erEntityUid = clazz.clazzUid
            entity?.erTableId = Clazz.TABLE_ID
            entity?.entityRoleScopeName =
                    clazz.clazzName
            mDataBinding?.fragmentEntityroleEditScopeTiet?.setText(
                    clazz.clazzName )
            mDataBinding?.entityRole = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java) {
            val person = it.firstOrNull() ?: return@observeResult
            entity?.erEntityUid = person.personUid
            entity?.erTableId = Person.TABLE_ID
            entity?.entityRoleScopeName =
                    person.firstNames + ' ' + person.lastName
            mDataBinding?.fragmentEntityroleEditScopeTiet?.setText(
                    person.firstNames + ' ' + person.lastName )
            mDataBinding?.entityRole = entity
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding = null
    }

    companion object {


    }
}
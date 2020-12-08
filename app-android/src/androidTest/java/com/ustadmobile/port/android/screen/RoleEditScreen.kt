package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.FragmentScenario
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.RoleEditFragment

object RoleEditScreen : KScreen<RoleEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_role_edit
    override val viewClass: Class<*>?
        get() = RoleEditFragment::class.java

    val editNameLayout = KTextInputLayout { withId(R.id.role_edit_permission_role_name_til)}


    fun fillFields(fragmentScenario: FragmentScenario<RoleEditFragment>,
                   role: Role, roleOnForm: Role?) {

        role.roleName?.takeIf { it != roleOnForm?.roleName }?.also {
            editNameLayout{
                edit{
                    replaceText(it)
                }
            }
        }

        //TODO: Bitmask


    }

}
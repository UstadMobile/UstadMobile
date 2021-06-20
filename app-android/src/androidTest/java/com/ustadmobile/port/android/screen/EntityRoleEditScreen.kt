package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.EntityRoleEditFragment

object EntityRoleEditScreen : KScreen<EntityRoleEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_entityrole_edit
    override val viewClass: Class<*>?
        get() = EntityRoleEditFragment::class.java

    val editRoleLayout = KTextInputLayout { withId(R.id.fragment_entityrole_edit_role_til)}
}
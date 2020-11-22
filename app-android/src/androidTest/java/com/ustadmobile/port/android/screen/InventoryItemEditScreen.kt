package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.InventoryItemEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object InventoryItemEditScreen : KScreen<InventoryItemEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_inventory_item_edit
    override val viewClass: Class<*>?
        get() = InventoryItemEditFragment::class.java



    fun fillFields(fragmentScenario: FragmentScenario<InventoryItemEditFragment>) {
        //TODO: set these values on the form using Espresso.



    }


}
package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SaleEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object SaleEditScreen : KScreen<SaleEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = SaleEditFragment::class.java

    val SaleTitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}


    fun fillFields(fragmentScenario: FragmentScenario<SaleEditFragment>,
                   sale: Sale,
                   saleOnForm: Sale?,
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context,
                   testContext: TestContext<Unit>) {
        //TODO: set these values on the form using Espresso.

        sale.saleName?.takeIf {it != saleOnForm?.saleName }?.also {
            SaleTitleInput{
                edit{
                    clearText()
                    typeText(it)
                }
            }
        }

        if(!setFieldsRequiringNavigation) {
            return
        }

        //TODO: if required, use the savedstatehandle to add link entities

        fragmentScenario.onFragment { fragment ->
            fragment.takeIf {sale.relatedEntity != saleOnForm?.relatedEntity }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(sale.relatedEntity)))
        }

    }


}
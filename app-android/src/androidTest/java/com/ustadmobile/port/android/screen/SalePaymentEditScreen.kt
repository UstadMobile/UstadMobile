package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SalePaymentEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object SalePaymentEditScreen : KScreen<SalePaymentEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = SalePaymentEditFragment::class.java

    val SalePaymentTitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}


    fun fillFields(fragmentScenario: FragmentScenario<SalePaymentEditFragment>,
                   salePayment: SalePayment,
                   salePaymentOnForm: SalePayment?,
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context,
                   testContext: TestContext<Unit>) {
        //TODO: set these values on the form using Espresso.

        salePayment.salePaymentName?.takeIf {it != salePaymentOnForm?.salePaymentName }?.also {
            SalePaymentTitleInput{
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
            fragment.takeIf {salePayment.relatedEntity != salePaymentOnForm?.relatedEntity }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(salePayment.relatedEntity)))
        }

    }


}
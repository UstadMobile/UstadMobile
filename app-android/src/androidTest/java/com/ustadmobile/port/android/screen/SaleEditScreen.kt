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
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.SalePayment

object SaleEditScreen : KScreen<SaleEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_sale_edit
    override val viewClass: Class<*>?
        get() = SaleEditFragment::class.java

    //val SaleTitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}
    val customerInput = KTextInputLayout { withId(R.id.fragment_sale_edit_customer_til)}
    val provinceInput = KTextInputLayout { withId(R.id.fragment_sale_edit_province_til)}
    val orderNotesInput = KTextInputLayout { withId(R.id.fragment_sale_edit_notes_til)}


    fun fillFields(fragmentScenario: FragmentScenario<SaleEditFragment>,
                   sale: Sale,
                   saleOnForm: Sale?,
                   saleItems: List<SaleItemWithProduct> = listOf(),
                   saleItemsOnForm: List<SaleItemWithProduct> = listOf(),
                   saleDeliveries: List<SaleDelivery> = listOf(),
                   saleDeliveriesOnForm: List<SaleDelivery> = listOf(),
                   salePayments: List<SalePayment> = listOf(),
                   salePaymentsOnForm: List<SalePayment> = listOf()) {
        //TODO: set these values on the form using Espresso.

        sale.saleNotes?.takeIf { it != saleOnForm?.saleNotes }?.also {
            orderNotesInput{
                edit{
//                    clearText()
//                    typeText(it)
                    replaceText(it)
                }
            }
        }

        saleItems.filter {  saleItemsOnForm == null || it !in saleItemsOnForm}.forEach{
            saleItem ->
            fragmentScenario.onFragment {
                it.findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set("SaleItem", defaultGson().toJson(listOf(saleItem)))
            }
        }

        saleDeliveries.filter {  saleDeliveriesOnForm == null || it !in saleDeliveriesOnForm}.forEach{
            saleDelivery ->
            fragmentScenario.onFragment {
                it.findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set("SaleDelivery", defaultGson().toJson(listOf(saleDelivery)))
            }
        }

        salePayments.filter {  salePaymentsOnForm == null || it !in salePaymentsOnForm}.forEach{
            salePayment ->
            fragmentScenario.onFragment {
                it.findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set("SalePayment", defaultGson().toJson(listOf(salePayment)))
            }
        }

        //TODO: if required, use the savedstatehandle to add link entities

//        fragmentScenario.onFragment { fragment ->
//            fragment.takeIf {sale.relatedEntity != saleOnForm?.relatedEntity }
//                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
//                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(sale.relatedEntity)))
//        }

    }


}
package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SaleItemEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.test.port.android.util.setDateField

object SaleItemEditScreen : KScreen<SaleItemEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = SaleItemEditFragment::class.java

    val SaleItemQuantityInput = KTextInputLayout { withId(R.id.fragment_sale_item_edit_quantity_til)}
    val SaleItemPricePerPieceInput = KTextInputLayout { withId(R.id.fragment_sale_item_edit_priceeach_til)}

    fun fillFields(fragmentScenario: FragmentScenario<SaleItemEditFragment>,
                   saleItem: SaleItemWithProduct,
                   saleItemOnForm: SaleItemWithProduct?) {

        saleItem.saleItemQuantity?.takeIf {it != saleItemOnForm?.saleItemQuantity }?.also {
            SaleItemQuantityInput{
                edit{
                    clearText()
                    typeText(it.toString())
                }
            }
        }

        saleItem.saleItemPricePerPiece?.takeIf {it != saleItemOnForm?.saleItemPricePerPiece }?.also {
            SaleItemPricePerPieceInput{
                edit{
                    clearText()
                    typeText(it.toString())
                }
            }
        }

        saleItem.saleItemDueDate.takeIf { it != saleItemOnForm?.saleItemDueDate }?.also {
            setDateField(R.id.fragment_sale_item_edit_delivery_date_til, it)
        }


    }


}
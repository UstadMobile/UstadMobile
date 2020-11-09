package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.port.android.view.ProductEditFragment

object ProductEditScreen : KScreen<ProductEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_product_edit
    override val viewClass: Class<*>?
        get() = ProductEditFragment::class.java

    val ProductTitleInput = KTextInputLayout { withId(R.id.fragment_product_edit_title_eng)}


    fun fillFields(fragmentScenario: FragmentScenario<ProductEditFragment>,
                   product: Product,
                   productOnForm: Product?,
                   categories: List<Category> = listOf(),
                   categoriesOnForm: List<Category>? = null,
                   setFieldsRequiringNavigation: Boolean = true) {

        product.productName?.takeIf {it != productOnForm?.productName }?.also {
            ProductTitleInput{
                edit{
                    replaceText(it)
//                    clearText()
//                    typeText(it)
                }
            }
        }

        if(!setFieldsRequiringNavigation) {
            return
        }

        //TODO: if required, use the savedstatehandle to add link entities

        categories.filter {  categoriesOnForm == null || it !in categoriesOnForm}.forEach{category ->
            fragmentScenario.onFragment {
                it.findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set("Cateogy", defaultGson().toJson(listOf(categories)))
            }
        }
//        fragmentScenario.onFragment { fragment ->
//            fragment.takeIf {product.relatedEntity != productOnForm?.relatedEntity }
//                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
//                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(product.relatedEntity)))
//        }

    }


}
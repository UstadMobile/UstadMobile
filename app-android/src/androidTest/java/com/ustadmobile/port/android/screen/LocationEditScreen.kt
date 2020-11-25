package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LocationEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object LocationEditScreen : KScreen<LocationEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = LocationEditFragment::class.java

    val LocationTitleInput = KTextInputLayout { withId(R.id.id_of_textfield_input_layout)}


    fun fillFields(fragmentScenario: FragmentScenario<LocationEditFragment>,
                   location: Location,
                   locationOnForm: Location?,
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context,
                   testContext: TestContext<Unit>) {
        //TODO: set these values on the form using Espresso.

        location.locationName?.takeIf {it != locationOnForm?.locationName }?.also {
            LocationTitleInput{
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
            fragment.takeIf {location.relatedEntity != locationOnForm?.relatedEntity }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("RelatedEntityName", defaultGson().toJson(listOf(location.relatedEntity)))
        }

    }


}
package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzWorkEditFragment

object ClazzWorkEditScreen : KScreen<ClazzWorkEditScreen>(){
    override val layoutId: Int?
        get() = R.layout.fragment_clazz_work_edit
    override val viewClass: Class<*>?
        get() = ClazzWorkEditFragment::class.java



}
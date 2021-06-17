package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.common.builders.ViewBuilder
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.switch.KSwitch
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ScopedGrantEditFragment
import org.hamcrest.Matcher

object ScopedGrantEditScreen : KScreen<ScopedGrantEditScreen>() {

    class BitmaskItem(parent: Matcher<View>) : KRecyclerItem<BitmaskItem>(parent) {

    }


    override val layoutId: Int
        get() = R.layout.fragment_scoped_grant_edit

    override val viewClass: Class<*>?
        get() = ScopedGrantEditFragment::class.java


    val recycler: KRecyclerView  = KRecyclerView({
        withId(R.id.fragment_scoped_grant_edit_recycler_view)
    }, itemTypeBuilder = {
        itemType(::BitmaskItem)
    })

    /**
     * Use this to find a permission switch. The normal recycleritem does not seem to work, as the
     * switch itself is the root of the recycler item view.
     *
     */
    fun permissionSwitch(function: ViewBuilder.() -> Unit) : KSwitch {
        return KSwitch() {
            isDescendantOfA {
                withId(R.id.fragment_scoped_grant_edit_recycler_view)
            }
            function()
        }
    }

}
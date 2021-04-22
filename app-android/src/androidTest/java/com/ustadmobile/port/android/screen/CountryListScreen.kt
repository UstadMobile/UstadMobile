package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.CountryListFragment
import org.hamcrest.Matcher

object CountryListScreen : KScreen<CountryListScreen>() {
    override val layoutId: Int?
        get() =  R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = CountryListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Country)
    })

    class Country(parent: Matcher<View>) : KRecyclerItem<Country>(parent) {
        val countryName = KView(parent) { withId(R.id.item_country_text) }
    }

}
package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.AccountListFragment
import org.hamcrest.Matcher

object AccountListScreen : KScreen<AccountListScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_account_list
    override val viewClass: Class<*>?
        get() = AccountListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.account_list_recycler)
    }, itemTypeBuilder = {
        itemType(::MainItem)
        itemType(::NewLayout)
        itemType(::AboutItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val profileButton: KButton = KButton(parent) { withId(R.id.account_profile) }
        val logoutButton = KButton(parent) { withId(R.id.account_logout) }
        val fullNameText = KTextView(parent) { withId(R.id.person_full_name) }
        val accountDeleteButton = KButton(parent) { withId(R.id.account_delete_icon)}
    }

    class NewLayout(parent: Matcher<View>) : KRecyclerItem<NewLayout>(parent) {
        val newLayout = KView { withId(R.id.item_createnew_layout) }
    }
    class AboutItem(parent: Matcher<View>) : KRecyclerItem<AboutItem>(parent) {
        val aboutTextView = KTextView { withId(R.id.account_about) }
    }
}
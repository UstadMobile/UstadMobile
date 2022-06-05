package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_GO_TO_COMPLETE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class ChatListPresenter(context: Any, arguments: Map<String, String>, view: ChatListView,
                        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ChatListView, Chat>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted{


    var searchText: String? = null


    var loggedInPersonUid = 0L


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        loggedInPersonUid = accountManager.activeAccount.personUid

        updateListOnView()

    }

    private fun updateListOnView() {
        view.list = repo.chatDao.findAllChatsForUser(
                searchText.toQueryLikeParam(), loggedInPersonUid)
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    override fun handleClickEntry(entry: Chat) {
        val chatWithLatestMessageAndCount: ChatWithLatestMessageAndCount = entry as ChatWithLatestMessageAndCount
        when(mListMode) {
            ListViewMode.PICKER -> finishWithResult(safeStringify(di,
                ListSerializer(Chat.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(ChatDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.chatUid.toString(),
                            ARG_PERSON_UID to chatWithLatestMessageAndCount.otherPersonUid.toString()
                        ), context)
        }
    }

    fun handleClickCreateNewFab(destinationResultKey: String?) {

        val args: Map<String, String>? = mapOf(
            ARG_GO_TO_COMPLETE to ChatDetailView.VIEW_NAME,
            PersonListPresenter.ARG_HIDE_PERSON_ADD to "true",
            PersonListView.ARG_EXCLUDE_PERSONUIDS_LIST to loggedInPersonUid.toString()
        )
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                PersonListView.VIEW_NAME,
                Person::class,
                Person.serializer(),
                destinationResultKey,
                true,
                arguments = args?.toMutableMap() ?: mutableMapOf(),
            )
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                ChatDetailView.VIEW_NAME,
                Chat::class,
                Chat.serializer(),
                destinationResultKey ?: CHAT_RESULT_KEY,
                arguments = args?.toMutableMap() ?: arguments.toMutableMap()
            )
        )
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickCreateNewFab() {}

    companion object {

        const val CHAT_RESULT_KEY = "Chat"
        const val CHAT_RESULT_KEY_GROUP = "ChatGroup"


    }



}
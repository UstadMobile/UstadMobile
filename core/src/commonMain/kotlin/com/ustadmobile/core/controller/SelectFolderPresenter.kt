package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.SelectFolderView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI

class SelectFolderPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: SelectFolderView,
    di: DI): UstadBaseController<SelectFolderView>(context, arguments, view, di)  {


    fun handleUriSelected(uri: String?){

        when {
            uri == null -> {
                requireNavController().currentBackStackEntry?.viewName?.let {
                    requireNavController().popBackStack(
                        it,true)
                }
            }
            (arguments[UstadView.ARG_RESULT_DEST_VIEWNAME] == ContentEntryEditViewModel.DEST_NAME) -> {
                finishWithResult(
                    safeStringify(di,
                        ListSerializer(String.serializer()),
                        listOf(uri))
                )
            }
            else -> {
                val args = mutableMapOf<String, String>()
                args[ContentEntryEditViewModel.ARG_URI] = uri
                args.putFromOtherMapIfPresent(arguments, UstadView.ARG_LEAF)
                args.putFromOtherMapIfPresent(arguments, UstadView.ARG_PARENT_ENTRY_UID)

                navigateForResult(NavigateForResultOptions(
                    this, null,
                    ContentEntryEditViewModel.DEST_NAME,
                    ContentEntry::class,
                    ContentEntry.serializer(),
                    arguments = args)
                )
            }

        }


    }








}
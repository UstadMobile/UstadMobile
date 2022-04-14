package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI

class SelectFilePresenterCommon(
    context: Any,
    arguments: Map<String, String>,
    view: SelectFileView,
    di: DI): UstadBaseController<SelectFileView>(context, arguments, view, di)  {


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.acceptedMimeTypes =  arguments[SelectFileView.ARG_MIMETYPE_SELECTED].toString().split(";")
    }

    fun handleUriSelected(uri: String?){

        when {
            uri == null -> {
                ustadNavController.currentBackStackEntry?.viewName?.let {
                    ustadNavController.popBackStack(
                        it,true)
                }
            }
            (arguments[UstadView.ARG_RESULT_DEST_VIEWNAME] == ContentEntryEdit2View.VIEW_NAME)
                    || arguments[UstadView.ARG_RESULT_DEST_VIEWNAME] == ClazzAssignmentDetailView.VIEW_NAME -> {
                finishWithResult(
                    safeStringify(di,
                    ListSerializer(String.serializer()),
                    listOf(uri))
                )
            }
            else -> {
                val args = mutableMapOf<String, String>()
                args[ContentEntryEdit2View.ARG_URI] = uri
                args.putFromOtherMapIfPresent(arguments, UstadView.ARG_LEAF)
                args.putFromOtherMapIfPresent(arguments, UstadView.ARG_PARENT_ENTRY_UID)

                navigateForResult(NavigateForResultOptions(
                    this, null,
                    ContentEntryEdit2View.VIEW_NAME,
                    ContentEntry::class,
                    ContentEntry.serializer(),
                    arguments = args)
                )
            }
        }


    }








}
package com.ustadmobile.core.controller

import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.UstadView
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
                requireNavController().currentBackStackEntry?.viewName?.let {
                    requireNavController().popBackStack(
                        it,true)
                }
            }
            (arguments[UstadView.ARG_RESULT_DEST_VIEWNAME] == ClazzAssignmentDetailView.VIEW_NAME) -> {
                finishWithResult(
                    safeStringify(di,
                    ListSerializer(String.serializer()),
                    listOf(uri))
                )
            }
        }


    }








}
package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.Hashtable;

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_CONTENT_ENTRY_UID = "contentEntryUid";

    public DownloadDialogPresenter(Object context, Hashtable arguments, DownloadDialogView view) {
        super(context, arguments, view);
    }



}

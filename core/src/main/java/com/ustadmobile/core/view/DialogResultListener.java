package com.ustadmobile.core.view;

import java.util.Hashtable;

/**
 *
 *
 * Created by mike on 4/16/17.
 */

public interface DialogResultListener {

    /**
     *
     * @param commandId
     * @param args
     */
    void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args);

}

package com.ustadmobile.port.android.view;

import android.support.v4.app.Fragment;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 * Created by mike on 10/15/15.
 */
public class UstadBaseFragment  extends Fragment{

    private String mUILocale;

    private UstadBaseController baseController;

    protected void setBaseController(UstadBaseController baseController) {
        this.baseController = baseController;
    }

    @Override
    public void onResume() {
        super.onResume();
        String sysLocale = UstadMobileSystemImpl.getInstance().getLocale();
        if(mUILocale != null && !mUILocale.equals(sysLocale)) {
            //the locale has changed - we need to update the ui
            baseController.setUIStrings();
        }

        mUILocale = new String(sysLocale);
    }


}

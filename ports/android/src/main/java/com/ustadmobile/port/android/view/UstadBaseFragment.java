package com.ustadmobile.port.android.view;

import android.support.v4.app.Fragment;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
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


    public int getDirection() {
        if(getActivity() instanceof UstadBaseActivity) {
            return ((UstadBaseActivity)getActivity()).getDirection();
        }else {
            return UstadMobileConstants.DIR_LTR;
        }
    }


    public void setDirection(int dir) {
        if(getActivity() instanceof UstadBaseActivity) {
            ((UstadBaseActivity)getActivity()).setDirection(dir);
        }
    }

    public void setAppMenuCommands(String[] labels, int[] ids) {
        if(getActivity() instanceof UstadBaseActivity) {
            ((UstadBaseActivity)getActivity()).setAppMenuCommands(labels, ids);
        }
    }




}

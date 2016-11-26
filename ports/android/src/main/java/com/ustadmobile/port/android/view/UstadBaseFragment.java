package com.ustadmobile.port.android.view;

import android.support.v4.app.Fragment;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;

/**
 * Created by mike on 10/15/15.
 */
public class UstadBaseFragment  extends Fragment{

    private String mUILocale;

    private UstadBaseController baseController;

    protected void setBaseController(UstadBaseController baseController) {
        this.baseController = baseController;
    }

    /**
     * Make a new instance of a base fragment with a hastable for arguments
     *
     * @param args Hashtable arguments (normally used by the to be created view controller)
     * @param type
     * @param <T>
     * @return
     */
    public static <T extends UstadBaseFragment> T newInstance(Hashtable args, Class<T> type) {
        try {
            UstadBaseFragment baseFrag = type.newInstance();
            baseFrag.setArguments(UMAndroidUtil.hashtableToBundle(args));
            return type.cast(baseFrag);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
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
        if(baseController != null)
            baseController.handleViewResume();
    }

    @Override
    public void onDestroy() {
        if(baseController != null)
            baseController.handleViewDestroy();
        super.onDestroy();
    }

    //TODO: add stop, pause handling here

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

    public void setUIStrings() {

    }

    public void setAppMenuCommands(String[] labels, int[] ids) {
        if(getActivity() instanceof UstadBaseActivity) {
            ((UstadBaseActivity)getActivity()).setAppMenuCommands(labels, ids);
        }
    }

    public void setAppStatus(int status)  {
        //not implemented yet
    }

    public void setDisplayName(String displayName) {
        //not implemented yet
    }




}

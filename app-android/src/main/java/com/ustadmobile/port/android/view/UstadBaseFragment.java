package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mike on 10/15/15.
 */
public class UstadBaseFragment  extends Fragment{

    private String mUILocale;

    private UstadBaseController baseController;

    private Vector<Runnable> runOnAttach = new Vector<>();

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
        String sysLocale = UstadMobileSystemImpl.getInstance().getLocale(getContext());
        if(mUILocale != null && !mUILocale.equals(sysLocale)) {
            //the locale has changed - we need to update the ui
            baseController.setUIStrings();
        }

        mUILocale = new String(sysLocale);
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

    /**
     * UstadBaseActivity overrides the onBackPressed and will ask all visible fragments if they want
     * to override the back button press.  This could be used to handle a back button press
     * on an internal browser or to close a menu etc.
     *
     * @return true if the fragment can go back and wants to addAuthHeader the back button press, false otherwise
     */
    public boolean canGoBack() {
        return false;
    }

    /**
     * UstadBaseActivity will call this method if canGoBack returned true.  This can be used to
     * go back in an internal webview or close a menu for example.
     */
    public void goBack() {

    }

    public void runOnUiThread(Runnable r) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(r);
        }else {
            runOnAttach.add(r);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Iterator<Runnable> runnables = runOnAttach.iterator();
        while(runnables.hasNext()) {
            Runnable current = runnables.next();
            current.run();
            runnables.remove();
        }
    }
}

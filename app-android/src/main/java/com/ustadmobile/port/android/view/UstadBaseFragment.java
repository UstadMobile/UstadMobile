package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.v4.app.Fragment;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mike on 10/15/15.
 */
public class UstadBaseFragment extends Fragment{

    private Vector<Runnable> runOnAttach = new Vector<>();

    @Override
    public void onDestroy() {
        super.onDestroy();
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

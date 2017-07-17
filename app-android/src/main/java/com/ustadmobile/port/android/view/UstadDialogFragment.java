package com.ustadmobile.port.android.view;

import android.content.Context;

import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mike on 7/17/17.
 */

public class UstadDialogFragment extends android.support.v4.app.DialogFragment implements DismissableDialog{

    protected DialogResultListener mResultListener;

    private Vector<Runnable> runOnAttach = new Vector<>();

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
        if(context instanceof DialogResultListener) {
            mResultListener = (DialogResultListener)context;
        }

        Iterator<Runnable> runnables = runOnAttach.iterator();
        while(runnables.hasNext()) {
            Runnable current = runnables.next();
            current.run();
            runnables.remove();
        }
    }

    public int getDirection() {
        return 0;
    }

    public void setDirection(int dir) {

    }

    public void setAppMenuCommands(String[] labels, int[] ids) {

    }

    public void setUIStrings() {

    }

}

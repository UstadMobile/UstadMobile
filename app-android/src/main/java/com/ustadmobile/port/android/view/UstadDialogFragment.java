package com.ustadmobile.port.android.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.impl.ReminderReceiver;

import java.util.Iterator;
import java.util.Vector;

import static android.content.Context.ALARM_SERVICE;
import static com.ustadmobile.port.android.view.UstadBaseActivity.ACTION_REMINDER_NOTIFICATION;

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


}

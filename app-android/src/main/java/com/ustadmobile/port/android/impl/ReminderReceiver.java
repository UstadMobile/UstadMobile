package com.ustadmobile.port.android.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.toughra.ustadmobile.R;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_NAME;
import static com.ustadmobile.port.android.view.UstadBaseActivity.ACTION_REMINDER_NOTIFICATION;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String saleName = intent.getExtras().get(ARG_SALE_ITEM_NAME).toString();
        showNotification(saleName, context);
    }
    public void showNotification(String saleTitle, Context context) {
        Intent intent = new Intent(context, getClass());

        PendingIntent pi = PendingIntent.getActivity(context, ACTION_REMINDER_NOTIFICATION, intent, 0);
        String message = context.getText(R.string.sale_due_today) + " " + saleTitle;
        String title = context.getText(R.string.sale_reminder).toString();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.goldozi_logo1)
                .setContentTitle(title)
                .setContentText(message);
        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ACTION_REMINDER_NOTIFICATION, mBuilder.build());
    }

}

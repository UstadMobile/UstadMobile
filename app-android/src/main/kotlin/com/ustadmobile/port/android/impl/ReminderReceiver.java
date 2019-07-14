package com.ustadmobile.port.android.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.util.UMCalendarUtil;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_DUE_DAYS;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_DUE_DATE;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_NAME;
import static com.ustadmobile.port.android.view.UstadBaseActivity.ACTION_REMINDER_NOTIFICATION;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String saleName = "";
        if(intent.getExtras().containsKey(ARG_SALE_ITEM_NAME)){
            if(intent.getExtras().get(ARG_SALE_ITEM_NAME) != null) {
                saleName = intent.getExtras().get(ARG_SALE_ITEM_NAME).toString();
            }
        }

        long dueDate = 0;

        if(intent.getExtras().containsKey(ARG_SALE_ITEM_DUE_DATE)){
            if(intent.getExtras().get(ARG_SALE_ITEM_DUE_DATE) != null) {
                dueDate = Long.parseLong(intent.getExtras().get(ARG_SALE_ITEM_DUE_DATE).toString());
            }
        }
        int days = 0;
        if(intent.getExtras().containsKey(ARG_SALE_DUE_DAYS)){
            if(intent.getExtras().get(ARG_SALE_DUE_DAYS) != null) {
                days = Integer.parseInt(intent.getExtras().get(ARG_SALE_DUE_DAYS).toString());
            }
        }

        showNotification(saleName, days, dueDate, context);
    }
    public void showNotification(String saleTitle, int days, long dueDate, Context context) {
        String prettyDueDate = UMCalendarUtil.getPrettyDateSimpleFromLong(dueDate, null);
        Intent intent = new Intent(context, getClass());
        PendingIntent pi = PendingIntent.getActivity(context, ACTION_REMINDER_NOTIFICATION, intent, 0);
        String message = context.getText(R.string.due_in) + " " + days + " " +
                context.getText(R.string.days) + " (" + prettyDueDate + ") " + context.getText(R.string.is_sale) +
                " " + saleTitle;
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
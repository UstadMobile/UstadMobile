package com.ustadmobile.port.android.impl

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.UMCalendarUtil

import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_DUE_DAYS
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DUE_DATE
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_NAME
import com.ustadmobile.port.android.view.UstadBaseActivity.Companion.ACTION_REMINDER_NOTIFICATION

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        var saleName = ""
        if (intent.extras!!.containsKey(ARG_SALE_ITEM_NAME)) {
            if (intent.extras!!.get(ARG_SALE_ITEM_NAME) != null) {
                saleName = intent.extras!!.get(ARG_SALE_ITEM_NAME)!!.toString()
            }
        }

        var dueDate: Long = 0

        if (intent.extras!!.containsKey(ARG_SALE_ITEM_DUE_DATE)) {
            if (intent.extras!!.get(ARG_SALE_ITEM_DUE_DATE) != null) {
                dueDate = java.lang.Long.parseLong(intent.extras!!.get(ARG_SALE_ITEM_DUE_DATE)!!.toString())
            }
        }
        var days = 0
        if (intent.extras!!.containsKey(ARG_SALE_DUE_DAYS)) {
            if (intent.extras!!.get(ARG_SALE_DUE_DAYS) != null) {
                days = Integer.parseInt(intent.extras!!.get(ARG_SALE_DUE_DAYS)!!.toString())
            }
        }

        showNotification(saleName, days, dueDate, context)
    }

    fun showNotification(saleTitle: String, days: Int, dueDate: Long, context: Context) {
        val prettyDueDate = UMCalendarUtil.getPrettyDateSimpleFromLong(dueDate, null)
        val intent = Intent(context, javaClass)
        val pi = PendingIntent.getActivity(context, ACTION_REMINDER_NOTIFICATION, intent, 0)
        val message = context.getText(R.string.due_in).toString() + " " + days + " " +
                context.getText(R.string.days) + " (" + prettyDueDate + ") " + context.getText(R.string.is_sale) +
                " " + saleTitle
        val title = context.getText(R.string.sale_reminder).toString()
        val mBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.goldozi_logo1)
                .setContentTitle(title)
                .setContentText(message)
        mBuilder.setContentIntent(pi)
        mBuilder.setDefaults(Notification.DEFAULT_SOUND)
        mBuilder.setAutoCancel(true)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(ACTION_REMINDER_NOTIFICATION, mBuilder.build())
    }

}
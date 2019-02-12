package com.ustadmobile.port.android.netwokmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpDownloadService extends Service {

    private class UmNotification {

        private int notificationId;

        private int downloadProgress = 0;

        private NotificationCompat.Builder mBuilder;

        UmNotification(int notificationId , NotificationCompat.Builder builder){
            this.mBuilder = builder;
            this.notificationId = notificationId;
        }


        public NotificationCompat.Builder getBuilder() {
            return mBuilder;
        }

        public int getDownloadProgress() {
            return downloadProgress;
        }

        public void setDownloadProgress(int downloadProgress) {
            this.downloadProgress = downloadProgress;
        }

        int getNotificationId() {
            return notificationId;
        }
    }


    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_CONTINUE_DOWNLOAD = "ACTION_CONTINUE_DOWNLOAD";

    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";

    public static final String NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID";

    public static final String JOB_ID_TAG = "UM_JOB_ID";

    public static final String KEY_NOTIFICATION_GROUP = "UM_NOTIFICATION_GROUP";

    public static final int MAX_PROGRESS_VALUE = 100;

    public static final int GROUP_SUMMARY_ID = 9;

    private NotificationManager mNotificationManager;

    private UmLiveData<List<DownloadJob>> activeDownloadJobData = null;

    private UmObserver<List<DownloadJob>> activeDownloadJobObserver;

    private HashMap<Long, UmNotification> knownNotifications = new HashMap<>();

    private AtomicInteger notificationIdRef = new AtomicInteger(GROUP_SUMMARY_ID);

    private Long timeLastUpdate;

    private long MAX_UPDATE_TIME_DELAY = TimeUnit.SECONDS.toMillis(5);

    private UmAppDatabase umAppDatabase;

    private UstadMobileSystemImpl impl;


    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager =
                (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);
        createChannel();

        umAppDatabase = UmAppDatabase.getInstance(this);
        activeDownloadJobData = umAppDatabase.getDownloadJobDao().getActiveDownloadJobs();
        activeDownloadJobObserver = this::handleJobListChanged;
        activeDownloadJobData.observeForever(activeDownloadJobObserver);
        impl = UstadMobileSystemImpl.getInstance();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.getAction() != null && intent.getExtras() != null){
            String action = intent.getAction();
            long downloadJobId = intent.getExtras().getLong(JOB_ID_TAG);
            UmNotification umNotification = knownNotifications.get(downloadJobId);

            switch (action){
                case ACTION_START_FOREGROUND_SERVICE:
                    timeLastUpdate = Calendar.getInstance().getTimeInMillis();
                    Notification groupSummary = createNotification(0L,notificationIdRef.get(),
                            "Summary title","Summary Content", true);
                    startForeground(notificationIdRef.get(), groupSummary);
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    break;
                case ACTION_CONTINUE_DOWNLOAD:
                    //TODO: Update database
                    break;
                case ACTION_CANCEL_DOWNLOAD:
                    if(umNotification != null){
                        cancelNotification(umNotification.getNotificationId());
                    }
                    //TODO: Update database
                    break;
            }
        }
        return START_STICKY;
    }

    private void handleJobListChanged(List<DownloadJob> activeJobs){
        for(DownloadJob job: activeJobs){
            long downloadJobId = job.getDjUid();
            UmNotification umNotification = knownNotifications.get(downloadJobId);

            if(umNotification == null && job.getDjStatus()!= JobStatus.COMPLETE){
                int notificationId = notificationIdRef.incrementAndGet();
                String message = impl.getString(MessageID.download_downloading_loading,
                        getApplicationContext());
                umAppDatabase.getDownloadJobDao().getEntryTitleByJobUid(downloadJobId,
                        new UmCallback<String>() {
                    @Override
                    public void onSuccess(String title) {
                        Notification download = createNotification(downloadJobId, notificationId,
                                title,message, false);
                        mNotificationManager.notify(notificationId,download);
                    }

                    @Override
                    public void onFailure(Throwable exception) {}
                });

            }else{
                if(job.getDjStatus() == JobStatus.COMPLETE){
                    cancelNotification(umNotification.getNotificationId());
                }else{
                    int progress = (int)((double)job.getBytesDownloadedSoFar()
                            / job.getTotalBytesToDownload() * 100);
                    Long timeCurrentUpdate = Calendar.getInstance().getTimeInMillis();
                    umNotification.setDownloadProgress(progress);
                    knownNotifications.put(downloadJobId,umNotification);

                    if(((timeCurrentUpdate - timeLastUpdate) < MAX_UPDATE_TIME_DELAY)
                            && umNotification.getDownloadProgress() > 0)
                        return;

                    timeLastUpdate = timeCurrentUpdate;
                    String message = String.format(impl.getString(
                            MessageID.download_downloading_placeholder,this),
                            UMFileUtil.formatFileSize(job.getBytesDownloadedSoFar()),
                            UMFileUtil.formatFileSize(job.getTotalBytesToDownload()));
                    updateDownloadNotification(downloadJobId, progress,message);
                }
            }
        }
    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
        mNotificationChannel.setVibrationPattern(new long[]{0});
        mNotificationChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
    }

    private NotificationCompat.Action createNotificationAction(long downloadJobId, String actionTag,
                                                               String actionLabel){
        Intent actionIntent = new Intent(this, HttpDownloadService.class);
        actionIntent.setAction(actionTag);
        actionIntent.putExtra(JOB_ID_TAG,downloadJobId);
        PendingIntent actionPendingIntent = PendingIntent.getService(this,
                0, actionIntent, 0);
        return new NotificationCompat.Action(0,actionLabel,actionPendingIntent);
    }

    public Notification createNotification(long downloadJobId, int notificationId,String title,
                                           String message, boolean isGroupSummary){

        Intent intent = new Intent();
        PendingIntent mNotificationPendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        Bitmap largeIconBitmap = getBitmap(R.drawable.ic_launch_green);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setLargeIcon(largeIconBitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(mNotificationPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setProgress(MAX_PROGRESS_VALUE,0,true)
                .setGroup(KEY_NOTIFICATION_GROUP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        if(isGroupSummary){
            String stopLabel = impl.getString(MessageID.download_cancel_stacked_label,
                    getApplicationContext());
            NotificationCompat.Action mStop = createNotificationAction(downloadJobId,
                    ACTION_STOP_FOREGROUND_SERVICE,stopLabel);
            builder.addAction(mStop).setGroupSummary(true);
        }else{
            String cancelLabel = impl.getString(MessageID.download_cancel_label,
                    getApplicationContext());
            NotificationCompat.Action mCancel = createNotificationAction(downloadJobId,
                    ACTION_CANCEL_DOWNLOAD,cancelLabel);

            String continueLabel = impl.getString(MessageID.download_continue_btn_label,
                    getApplicationContext());
            NotificationCompat.Action mContinue = createNotificationAction(downloadJobId,
                    ACTION_CONTINUE_DOWNLOAD,continueLabel);
            builder.addAction(mCancel).addAction(mContinue);
        }

        if(!knownNotifications.containsKey(downloadJobId) && !isGroupSummary){
            knownNotifications.put(downloadJobId,new UmNotification(notificationId,builder));
        }

        return builder.build();
    }

    private void cancelNotification(int notificationId){
        if(mNotificationManager != null){
            mNotificationManager.cancel(notificationId);
        }
    }

    private void updateDownloadNotification(long downloadJobId, int progress,String text){
        UmNotification umNotification = knownNotifications.get(downloadJobId);
        if(umNotification != null){
            umNotification.getBuilder()
                    .setContentText(text)
                    .setProgress(MAX_PROGRESS_VALUE,progress,false);
            mNotificationManager.notify(umNotification.getNotificationId(),
                    umNotification.getBuilder().build());
        }
    }


    private void stopForegroundService(){
        if(mNotificationManager != null){
            mNotificationManager.cancel(GROUP_SUMMARY_ID);
            stopForeground(true);
            stopSelf();
        }
    }


    private Bitmap getBitmap(int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        knownNotifications.clear();
        activeDownloadJobData.removeObserver(activeDownloadJobObserver);
    }
}

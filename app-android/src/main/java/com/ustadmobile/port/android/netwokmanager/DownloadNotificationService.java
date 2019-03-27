package com.ustadmobile.port.android.netwokmanager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static android.support.v4.app.NotificationCompat.GROUP_ALERT_SUMMARY;

/**
 * This services monitors the download job statuses and act accordingly
 */
public class DownloadNotificationService extends Service {

    /**
     * Holder class for the entire notification
     */
    private class UmNotification {

        private int notificationId;

        private int downloadProgress = 0;

        private String jobTitle;

        private NotificationCompat.Builder mBuilder;

        UmNotification(int notificationId ,String jobTitle, NotificationCompat.Builder builder){
            this.mBuilder = builder;
            this.notificationId = notificationId;
            this.jobTitle = jobTitle;
        }


        public NotificationCompat.Builder getBuilder() {
            return mBuilder;
        }

        int getDownloadProgress() {
            return downloadProgress;
        }

        void setDownloadProgress(int downloadProgress) {
            this.downloadProgress = downloadProgress;
        }

        int getNotificationId() {
            return notificationId;
        }

        String getJobTitle() {
            return jobTitle;
        }
    }


    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD";

    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";

    public static final String NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID";

    public static final String JOB_ID_TAG = "UM_JOB_ID";

    public static final String NOTIFICATION_GROUP_KEY = "com.android.example.UstadMobile";

    public static final int MAX_PROGRESS_VALUE = 100;

    public static final long GROUP_SUMMARY_ID = -1L;

    private long totalBytesToBeDownloaded;

    private long totalBytesDownloadedSoFar;


    private NotificationManagerCompat mNotificationManager;


    private UmLiveData<List<DownloadJob>> activeDownloadJobData = null;

    private UmObserver<List<DownloadJob>> activeDownloadJobObserver;

    private HashMap<Long, UmNotification> knownNotifications = new HashMap<>();

    private AtomicInteger notificationIdRef = new AtomicInteger(9);

    private Long timeLastUpdate;

    private long MAX_UPDATE_TIME_DELAY = TimeUnit.SECONDS.toMillis(2);

    private UmAppDatabase umAppDatabase;

    private UstadMobileSystemImpl impl;


    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = NotificationManagerCompat.from(this);
        createChannel();

        umAppDatabase = UmAppDatabase.getInstance(this);
        new Handler().postDelayed(() -> {
            activeDownloadJobData = umAppDatabase.getDownloadJobDao().getActiveDownloadJobs();
            activeDownloadJobObserver = DownloadNotificationService.this::handleJobListChanged;
            activeDownloadJobData.observeForever(activeDownloadJobObserver);
        },TimeUnit.SECONDS.toMillis(1));
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
                    String contentTitle = impl.getString(MessageID.downloading,
                            getApplicationContext());
                    Notification groupSummary = createNotification(GROUP_SUMMARY_ID,
                            notificationIdRef.get(), contentTitle, "", "",
                            true);
                    startForeground(notificationIdRef.get(), groupSummary);
                    break;

                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    break;

                case ACTION_PAUSE_DOWNLOAD:
                    if(umNotification != null){
                        new Thread(() -> umAppDatabase.getDownloadJobDao()
                                .updateJobAndItems(downloadJobId, JobStatus.PAUSED,
                                        JobStatus.PAUSING)).start();
                    }

                    break;

                case ACTION_CANCEL_DOWNLOAD:
                    if(umNotification != null){
                        new Thread(() -> umAppDatabase.getDownloadJobDao()
                                .updateJobAndItems(downloadJobId, JobStatus.CANCELED,
                                        JobStatus.CANCELLING)).start();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * Handle whn job items changes
     * @param activeJobs list of all jobs being monitored
     */
    private void handleJobListChanged(List<DownloadJob> activeJobs){
        List<Long> inactiveDownloadJobs = new ArrayList<>();
        for(Long knownDownloadId : knownNotifications.keySet()) {
            boolean activeJobsContainedKnownId = false;
            for(DownloadJob job : activeJobs) {
                if(job.getDjUid() == knownDownloadId) {
                    activeJobsContainedKnownId = true;
                    break;
                }
            }

            if(!activeJobsContainedKnownId) {
                inactiveDownloadJobs.add(knownDownloadId);
            }
        }

        for(Long inactiveDownloadUid : inactiveDownloadJobs) {
            if(mNotificationManager != null){
                mNotificationManager.cancel(knownNotifications.get(inactiveDownloadUid).notificationId);
            }

            knownNotifications.remove(inactiveDownloadUid);
        }

        for(DownloadJob job: activeJobs){
            long downloadJobId = job.getDjUid();
            UmNotification umNotification = knownNotifications.get(downloadJobId);
            boolean isRunning = job.getDjStatus() >= JobStatus.RUNNING_MIN
                    && job.getDjStatus() <= JobStatus.RUNNING_MAX;

            if(umNotification == null){
                totalBytesToBeDownloaded = totalBytesToBeDownloaded +
                        job.getTotalBytesToDownload();
                int notificationId = notificationIdRef.incrementAndGet();
                String contentTitle = String.format(impl.getString(
                        MessageID.download_downloading_placeholder,this),
                        UMFileUtil.formatFileSize(job.getBytesDownloadedSoFar()),
                        UMFileUtil.formatFileSize(job.getTotalBytesToDownload()));
                umAppDatabase.getDownloadJobDao().getEntryTitleByJobUid(downloadJobId,
                        new UmCallback<String>() {
                    @Override
                    public void onSuccess(String title) {
                        Notification download = createNotification(downloadJobId, notificationId,
                                title ,contentTitle,contentTitle,false);
                        mNotificationManager.notify(notificationId,download);
                    }

                    @Override
                    public void onFailure(Throwable exception) {}
                });

            } else {
                totalBytesDownloadedSoFar = totalBytesDownloadedSoFar +
                        job.getBytesDownloadedSoFar();
                int progress = (int)((double)job.getBytesDownloadedSoFar()
                        / job.getTotalBytesToDownload() * 100);
                Long timeCurrentUpdate = Calendar.getInstance().getTimeInMillis();
                umNotification.setDownloadProgress(progress);

                if(((timeCurrentUpdate - timeLastUpdate) < MAX_UPDATE_TIME_DELAY)
                        && umNotification.getDownloadProgress() > 0 && isRunning)
                    return;

                timeLastUpdate = timeCurrentUpdate;
                String contentTitle = String.format(impl.getString(
                        MessageID.download_downloading_placeholder,this),
                        UMFileUtil.formatFileSize(job.getBytesDownloadedSoFar()),
                        UMFileUtil.formatFileSize(job.getTotalBytesToDownload()));
                updateDownloadJobNotification(downloadJobId, progress,contentTitle,
                        umNotification.getJobTitle(), umNotification.getJobTitle());
                updateDownloadSummary();
            }
        }

        if(activeJobs.isEmpty()){
            stopForegroundService();
        }
    }

    /**
     * Create a channel for the notification
     */
    private void createChannel(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
        mNotificationChannel.setVibrationPattern(new long[]{0});
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setSound(null,null);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(mNotificationChannel);
    }

    /**
     * Create action buttons
     * @param downloadJobId Identifies the job on which buttons will appear
     * @param actionTag action tags for the prnding intent
     * @param actionLabel button label text
     * @return constructed action button
     */
    private NotificationCompat.Action createAction(long downloadJobId, String actionTag,
                                                   String actionLabel){
        Intent actionIntent = new Intent(this, DownloadNotificationService.class);
        actionIntent.putExtra(JOB_ID_TAG,downloadJobId);
        actionIntent.setAction(actionTag);
        PendingIntent actionPendingIntent = PendingIntent.getService(this,
                0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(0,actionLabel,actionPendingIntent);
    }

    /**
     * Construct a notification object
     * @param downloadJobId Identifies the job in which notification belongs
     * @param notificationId Notification Id
     * @param contentTitle Notification content title
     * @param contentText Notification content text
     * @param contentSubText Notification content sub text
     * @param isGroupSummary Flag to indicate if the notification will act as a group summary or not.
     * @return constructed notification object
     */
    public Notification createNotification(long downloadJobId, int notificationId,String contentTitle,
                                           String contentText, String contentSubText,
                                           boolean isGroupSummary){

        Intent intent = new Intent();
        PendingIntent mNotificationPendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setOngoing(true)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setAutoCancel(true)
                .setContentIntent(mNotificationPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND);

        UmNotification umNotification = new UmNotification(notificationId,contentTitle,builder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        if(isGroupSummary){
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .setBigContentTitle(contentTitle)
                    .setSummaryText(contentSubText);

            builder.setGroupSummary(true)
                    .setStyle(inboxStyle);
        }else{
            builder.setProgress(MAX_PROGRESS_VALUE,0,true)
                    .addAction(createAction(downloadJobId,
                    ACTION_CANCEL_DOWNLOAD, impl.getString(MessageID.download_cancel_label,
                            getApplicationContext())))
                    .addAction(createAction(downloadJobId,
                    ACTION_PAUSE_DOWNLOAD, impl.getString(MessageID.download_pause_download,
                            getApplicationContext())))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText);
        }
        builder.setGroup(NOTIFICATION_GROUP_KEY);

        if(!knownNotifications.containsKey(downloadJobId)){
            knownNotifications.put(downloadJobId,umNotification);
        }

        Notification notification = builder.build();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notification.defaults = 0;
            notification.sound = null;
        }

        return notification;
    }


    /**
     * Update download job notification
     * @param downloadJobId Id to indicate which job is the notification for
     * @param progress current download progress
     * @param contentTitle Notification content title
     * @param contentText Notification content text
     * @param contentSubText Notification content sub text
     */
    private void updateDownloadJobNotification(long downloadJobId, int progress, String contentTitle ,
                                               String contentText, String contentSubText){
        UmNotification umNotification = knownNotifications.get(downloadJobId);
        if(umNotification != null){
            NotificationCompat.Builder builder = umNotification.getBuilder();
            builder.setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText)
                    .setProgress(MAX_PROGRESS_VALUE,progress,false);
            mNotificationManager.notify(umNotification.getNotificationId(),
                    umNotification.getBuilder().build());
        }
    }

    /**
     * Update summary notification to show progress as the sum of all download notifications
     */
    private void updateDownloadSummary(){
        UmNotification umNotification = knownNotifications.get(GROUP_SUMMARY_ID);
        if(umNotification != null){
            String summaryLabel = impl.getString(MessageID.download_downloading_placeholder,
                    getApplicationContext());
            String title = String.format(summaryLabel,
                    UMFileUtil.formatFileSize(totalBytesDownloadedSoFar),
                    UMFileUtil.formatFileSize(totalBytesToBeDownloaded));
            totalBytesDownloadedSoFar = 0L;
            umNotification.getBuilder().setSubText(title);
            mNotificationManager.notify(umNotification.getNotificationId(),
                    umNotification.getBuilder().build());
        }
    }


    /**
     * Stop foreground service
     */
    private void stopForegroundService(){
        if(mNotificationManager != null){
            stopForeground(true);
            stopSelf();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        knownNotifications.clear();
        if(activeDownloadJobData != null)activeDownloadJobData.removeObserver(activeDownloadJobObserver);
    }
}

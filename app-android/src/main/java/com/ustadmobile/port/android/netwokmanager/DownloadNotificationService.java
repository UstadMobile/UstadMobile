package com.ustadmobile.port.android.netwokmanager;

import android.annotation.SuppressLint;
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
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

        private List<UmAction> knownActions = new ArrayList<>();

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

        UmNotification addAction(UmAction action){
            knownActions.add(action);
            return this;
        }

        List<UmAction> getKnownActions() {
            return knownActions;
        }
    }

    /**
     * Holder class for the notification action
     */
    private class UmAction{

        private String actionKey;

        private NotificationCompat.Action action;

        UmAction(String actionKey, NotificationCompat.Action action) {
            this.actionKey = actionKey;
            this.action = action;
        }

        String getActionKey() {
            return actionKey;
        }

        public NotificationCompat.Action getAction() {
            return action;
        }
    }


    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSE_ALL_DOWNLOADS = "ACTION_PAUSE_ALL_DOWNLOADS";

    public static final String ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD";

    public static final String ACTION_CANCEL_ALL_DOWNLOADS = "ACTION_CANCEL_ALL_DOWNLOADS";

    public static final String ACTION_CONTINUE_DOWNLOAD = "ACTION_CONTINUE_DOWNLOAD";

    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";

    public static final String NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID";

    public static final String JOB_ID_TAG = "UM_JOB_ID";

    public static final String KEY_NOTIFICATION_GROUP = "UM_NOTIFICATION_GROUP";

    public static final int MAX_PROGRESS_VALUE = 100;

    public static final long GROUP_SUMMARY_ID = 9L;

    private long totalBytesToBeDownloaded;

    private long totalBytesDownloadedSoFar;


    private NotificationManager mNotificationManager;

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

        mNotificationManager =
                (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);
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
                    String contentTitle = impl.getString(MessageID.download_calculating,
                            getApplicationContext());
                    Notification groupSummary = createNotification(GROUP_SUMMARY_ID,
                            notificationIdRef.get(), contentTitle, "", contentTitle,
                            true, ACTION_PAUSE_DOWNLOAD);
                    startForeground(notificationIdRef.get(), groupSummary);
                    break;
                case ACTION_CANCEL_ALL_DOWNLOADS:
                    //TODO: pause all download task
                    new Thread(() ->
                            umAppDatabase.getDownloadJobDao().updateAllJobsAndItems(
                                    JobStatus.CANCELLING)).start();
                    break;
                case ACTION_PAUSE_ALL_DOWNLOADS:
                    //TODO: pause all download task
                    new Thread(() ->
                            umAppDatabase.getDownloadJobDao().updateAllJobsAndItems(
                                    JobStatus.PAUSING)).start();
                    break;

                case ACTION_PAUSE_DOWNLOAD:
                    //TODO: pause all download task
                    new Thread(() ->
                            umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJobId,
                                    JobStatus.PAUSING, -1)).start();
                    break;
                case ACTION_CONTINUE_DOWNLOAD:
                    new Thread(() ->
                            umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJobId,
                            JobStatus.QUEUED, -1)).start();
                    break;
                case ACTION_CANCEL_DOWNLOAD:
                    if(umNotification != null){
                        new Thread(() ->
                                umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJobId,
                                JobStatus.CANCELLING, -1)).start();
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
        if(activeJobs.isEmpty()){
            stopForegroundService();
            return;
        }

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
            cancelNotification(knownNotifications.get(inactiveDownloadUid).notificationId);
            knownNotifications.remove(inactiveDownloadUid);
        }

        for(DownloadJob job: activeJobs){
            long downloadJobId = job.getDjUid();
            UmNotification umNotification = knownNotifications.get(downloadJobId);
            String keyToExclude = job.getDjStatus() >= JobStatus.RUNNING_MIN
                    && job.getDjStatus() <= JobStatus.RUNNING_MAX ?
                    ACTION_CONTINUE_DOWNLOAD : ACTION_PAUSE_DOWNLOAD;
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
                                title ,contentTitle,contentTitle,false, keyToExclude);
                        mNotificationManager.notify(notificationId,download);
                        System.out.print("Create notification called with job id ="+job.getDjUid());
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
                updateDownloadNotification(downloadJobId, progress,contentTitle,
                        umNotification.getJobTitle(), umNotification.getJobTitle(), keyToExclude);
                updateSummary();
                totalBytesDownloadedSoFar = 0L;
            }
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
        mNotificationManager.createNotificationChannel(mNotificationChannel);
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
        actionIntent.setAction(actionTag);
        actionIntent.putExtra(JOB_ID_TAG,downloadJobId);
        PendingIntent actionPendingIntent = PendingIntent.getService(this,
                0, actionIntent, 0);
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
     * @param keyToExclude Action button to be excluded when updating the notification view
     * @return constructed notification object
     */
    public Notification createNotification(long downloadJobId, int notificationId,String contentTitle,
                                           String contentText, String contentSubText,
                                           boolean isGroupSummary, String keyToExclude){

        Intent intent = new Intent();
        PendingIntent mNotificationPendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        Bitmap largeIconBitmap = getBitmap(R.drawable.ic_launch_green);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setLargeIcon(largeIconBitmap)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSubText(contentSubText)
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(mNotificationPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setStyle(new NotificationCompat.InboxStyle())
                .setGroup(KEY_NOTIFICATION_GROUP);

        UmNotification umNotification = new UmNotification(notificationId,contentTitle,builder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        if(isGroupSummary){
            builder.setProgress(MAX_PROGRESS_VALUE,0,true);
            UmAction mCancel = new UmAction(ACTION_CANCEL_ALL_DOWNLOADS, createAction(downloadJobId,
                    ACTION_CANCEL_ALL_DOWNLOADS, impl.getString(MessageID.download_cancel_all,
                            getApplicationContext())));

            UmAction mPause = new UmAction(ACTION_PAUSE_ALL_DOWNLOADS, createAction(downloadJobId,
                    ACTION_PAUSE_ALL_DOWNLOADS, impl.getString(MessageID.download_pause_all,
                            getApplicationContext())));
            umNotification.addAction(mCancel).addAction(mPause);
        }else{


            UmAction mContinue = new UmAction(ACTION_CONTINUE_DOWNLOAD, createAction(downloadJobId,
                    ACTION_CONTINUE_DOWNLOAD, impl.getString(MessageID.download_continue_btn_label,
                            getApplicationContext())));

            UmAction mCancel = new UmAction(ACTION_CANCEL_DOWNLOAD, createAction(downloadJobId,
                    ACTION_CANCEL_DOWNLOAD, impl.getString(MessageID.download_cancel_label,
                            getApplicationContext())));

            UmAction mPause = new UmAction(ACTION_PAUSE_DOWNLOAD, createAction(downloadJobId,
                    ACTION_PAUSE_DOWNLOAD, impl.getString(MessageID.download_pause_download,
                            getApplicationContext())));

            umNotification.addAction(mCancel).addAction(mContinue).addAction(mPause);
        }

        attachActionOnBuilder(builder,umNotification,keyToExclude);

        if(!knownNotifications.containsKey(downloadJobId)){
            knownNotifications.put(downloadJobId,umNotification);
        }


        return builder.build();
    }

    /**
     * Attache required action buttons to the notification builder
     * @param builder Builder to attach the button to
     * @param umNotification UmNotification object which has all actions list to be included
     * @param keyToExclude Action button to be excluded when updating the notification view
     */
    private void attachActionOnBuilder(NotificationCompat.Builder builder,
                                       UmNotification umNotification, String keyToExclude){
        for (UmAction action : umNotification.getKnownActions()) {
            if (!action.getActionKey().equals(keyToExclude)) {
                builder.addAction(action.getAction());
            }
        }
    }

    /**
     * Cancel specific job notification
     * @param notificationId notification id to be canceled.
     */
    private void cancelNotification(int notificationId){
        if(mNotificationManager != null){
            mNotificationManager.cancel(notificationId);
        }
    }


    /**
     * Update download job notification
     * @param downloadJobId Id to indicate which job is the notification for
     * @param progress current download progress
     * @param contentTitle Notification content title
     * @param contentText Notification content text
     * @param contentSubText Notification content sub text
     * @param actionToExclude Action button to be excluded when updating the notification view
     */
    @SuppressLint("RestrictedApi")
    private void updateDownloadNotification(long downloadJobId, int progress, String contentTitle ,
                                            String contentText, String contentSubText,
                                            String actionToExclude){
        UmNotification umNotification = knownNotifications.get(downloadJobId);
        if(umNotification != null){
            NotificationCompat.Builder builder = umNotification.getBuilder();
            builder.mActions.clear();
            builder.setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText)
                    .setProgress(MAX_PROGRESS_VALUE,progress,false);
            attachActionOnBuilder(builder,umNotification,actionToExclude);
            mNotificationManager.notify(umNotification.getNotificationId(),
                    umNotification.getBuilder().build());
        }
    }

    /**
     * Update summary notification to show progress as the sum of all download notifications
     */
    private void updateSummary(){
        String summaryLabel = impl.getString(MessageID.download_downloading_placeholder,
                getApplicationContext());
        int progress = (int)((double) totalBytesDownloadedSoFar / totalBytesToBeDownloaded * 100);
        String title = String.format(summaryLabel,
                UMFileUtil.formatFileSize(totalBytesDownloadedSoFar),
                UMFileUtil.formatFileSize(totalBytesToBeDownloaded));
        updateDownloadNotification(GROUP_SUMMARY_ID,progress,title,"","","");
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


    /**
     * Create bitmap from drawable resource
     * @param resource resource id
     * @return Constructed bitmap
     */
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

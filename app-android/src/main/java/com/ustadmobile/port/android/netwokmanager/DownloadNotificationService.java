package com.ustadmobile.port.android.netwokmanager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadJobItemManager;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.support.v4.app.NotificationCompat.GROUP_ALERT_SUMMARY;

/**
 * This services monitors the download job statuses and act accordingly
 */
public class DownloadNotificationService extends Service
        implements DownloadJobItemManager.OnDownloadJobItemChangeListener {

    /**
     * Holder class for the entire notification
     */
    private class NotificationHolder {

        private int notificationId;

        private int downloadProgress = 0;

        private String contentTitle;

        private NotificationCompat.Builder mBuilder;

        NotificationHolder(int notificationId , String contentTitle, NotificationCompat.Builder builder){
            this.mBuilder = builder;
            this.notificationId = notificationId;
            this.contentTitle = contentTitle;
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
            return contentTitle;
        }

        void setContentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            mBuilder.setContentTitle(contentTitle);
        }

        Notification build() {
            Notification notification = mBuilder.build();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                notification.defaults = 0;
                notification.sound = null;
            }

            return notification;
        }
    }

    private ServiceConnection mNetworkServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mNetworkServiceBound.set(true);
            networkManagerBle = ((NetworkManagerBleAndroidService.LocalServiceBinder) service)
                    .getService().getNetworkManagerBle();
            networkManagerBle.addDownloadChangeListener(DownloadNotificationService.this);
            List<DownloadJobItemManager> activeDownloadManagers = networkManagerBle
                    .getActiveDownloadJobItemManagers();
            for(DownloadJobItemManager manager : activeDownloadManagers) {
                onDownloadJobItemChange(manager.getRootItemStatus(), manager);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mNetworkServiceBound.set(false);
            if(networkManagerBle != null){
                networkManagerBle.removeDownloadChangeListener(DownloadNotificationService.this);
                networkManagerBle = null;
            }
        }
    };

    private AtomicBoolean mNetworkServiceBound = new AtomicBoolean(false);

    private NetworkManagerAndroidBle networkManagerBle;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD";

    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";

    public static final String NOTIFICATION_CHANNEL_ID = "UM_NOTIFICATION_CHANNEL_ID";

    public static final String JOB_ID_TAG = "UM_JOB_ID";

    public static final String NOTIFICATION_GROUP_KEY = "com.android.example.UstadMobile";

    public static final int MAX_PROGRESS_VALUE = 100;

    public static final int GROUP_SUMMARY_ID = -1;

    private long totalBytesToBeDownloaded;

    private long totalBytesDownloadedSoFar;

    private NotificationManagerCompat mNotificationManager;

    private final Map<Integer, NotificationHolder> downloadJobIdToNotificationMap = new HashMap<>();

    private AtomicInteger notificationIdRef = new AtomicInteger(9);

    private Long timeLastUpdate;

    private long MAX_UPDATE_TIME_DELAY = TimeUnit.SECONDS.toMillis(2);

    private UmAppDatabase umAppDatabase;

    private UstadMobileSystemImpl impl;

    private static final AtomicInteger SERVICE_ID_COUNTER = new AtomicInteger(0);

    private final int serviceInstanceId = SERVICE_ID_COUNTER.incrementAndGet();

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = NotificationManagerCompat.from(this);
        createChannel();

        umAppDatabase = UmAppDatabase.getInstance(this);

        //bind to network service
        Intent networkServiceIntent = new Intent(getApplicationContext(),
                NetworkManagerBleAndroidService.class);
        bindService(networkServiceIntent, mNetworkServiceConnection, Context.BIND_AUTO_CREATE);

        impl = UstadMobileSystemImpl.Companion.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.getAction() != null && intent.getExtras() != null){
            String action = intent.getAction();
            int downloadJobId = intent.getExtras().getInt(JOB_ID_TAG);
            NotificationHolder notificationHolder = downloadJobIdToNotificationMap.get(downloadJobId);

            switch (action){
                case ACTION_START_FOREGROUND_SERVICE:
                    timeLastUpdate = System.currentTimeMillis();
                    String contentTitle = impl.getString(MessageID.downloading,
                            getApplicationContext());
                    NotificationHolder notification = createNotification(GROUP_SUMMARY_ID,
                            notificationIdRef.get(), contentTitle, "", "",
                            canCreateGroupedNotification());
                    startForeground(notificationIdRef.get(), notification.build());
                    break;

                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    break;

                case ACTION_PAUSE_DOWNLOAD:
                    if(notificationHolder != null){
                        new Thread(() -> umAppDatabase.getDownloadJobDao()
                                .updateJobAndItems(downloadJobId, JobStatus.PAUSED,
                                        JobStatus.PAUSING)).start();
                    }

                    break;

                case ACTION_CANCEL_DOWNLOAD:
                    if(notificationHolder != null){
                        new Thread(() -> umAppDatabase.getDownloadJobDao()
                                .updateJobAndItems(downloadJobId, JobStatus.CANCELED,
                                        JobStatus.CANCELLING)).start();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public synchronized void onDownloadJobItemChange(@Nullable DownloadJobItemStatus status, @NotNull DownloadJobItemManager manager) {
        if(status != null && manager.getRootContentEntryUid() == status.getContentEntryUid()) {
            int downloadJobId = manager.getDownloadJobUid();
            NotificationHolder notificationHolder = downloadJobIdToNotificationMap.get(downloadJobId);
            boolean isRunning = status.getStatus() >= JobStatus.RUNNING_MIN
                    && status.getStatus() <= JobStatus.RUNNING_MAX;

            if(notificationHolder == null){
                UMLog.l(UMLog.VERBOSE, 699,
                        "Service #" + serviceInstanceId +
                                " Creating new notification for download #" + downloadJobId);
                totalBytesToBeDownloaded = totalBytesToBeDownloaded +
                        status.getTotalBytes();
                int notificationId = notificationIdRef.incrementAndGet();
                String contentTitle = String.format(impl.getString(
                        MessageID.download_downloading_placeholder,this),
                        UMFileUtil.INSTANCE.formatFileSize(status.getBytesSoFar()),
                        UMFileUtil.INSTANCE.formatFileSize(status.getTotalBytes()));
                NotificationHolder holder = createNotification(downloadJobId, notificationId,
                        "",contentTitle,contentTitle,false);
                downloadJobIdToNotificationMap.put(downloadJobId, holder);
                umAppDatabase.getDownloadJobDao().getEntryTitleByJobUid(downloadJobId,
                        new UmCallback<String>() {
                            @Override
                            public void onSuccess(String title) {
                                holder.setContentTitle(title);
                                mNotificationManager.notify(notificationId, holder.build());
                            }

                            @Override
                            public void onFailure(Throwable exception) {}
                        });

            }else if(status.getStatus() >= JobStatus.COMPLETE_MIN) {
                //job has completed and notification needs to be removed
                NotificationHolder notification = downloadJobIdToNotificationMap.get(manager.getDownloadJobUid());
                if(notification != null) {
                    mNotificationManager.cancel(notification.notificationId);
                    downloadJobIdToNotificationMap.remove(manager.getDownloadJobUid());
                    if(downloadJobIdToNotificationMap.isEmpty()) {
                        UMLog.l(UMLog.INFO, 699, "DownloadNotificationService: Stop");
                        stopForegroundService();
                    }
                }else {
                    UMLog.l(UMLog.ERROR, 699, "Cannot find notification for download!");
                }
            }else {
                totalBytesDownloadedSoFar = totalBytesDownloadedSoFar +
                        status.getBytesSoFar();
                int progress = (int)((double)status.getBytesSoFar()
                        / status.getTotalBytes() * 100);
                Long timeCurrentUpdate = Calendar.getInstance().getTimeInMillis();
                notificationHolder.setDownloadProgress(progress);

                if(((timeCurrentUpdate - timeLastUpdate) < MAX_UPDATE_TIME_DELAY)
                        && notificationHolder.getDownloadProgress() > 0 && isRunning)
                    return;

                timeLastUpdate = timeCurrentUpdate;
                String contentTitle = String.format(impl.getString(
                        MessageID.download_downloading_placeholder,this),
                        UMFileUtil.INSTANCE.formatFileSize(status.getBytesSoFar()),
                        UMFileUtil.INSTANCE.formatFileSize(status.getTotalBytes()));
                updateDownloadJobNotification(downloadJobId, progress,contentTitle,
                        notificationHolder.getJobTitle(), notificationHolder.getJobTitle());
                updateDownloadSummary();
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
    public NotificationHolder createNotification(long downloadJobId, int notificationId,String contentTitle,
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
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setOngoing(true)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setAutoCancel(true)
                .setContentIntent(mNotificationPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND);

        if(isVersionLollipopOrAbove()){
            builder.setSmallIcon(R.drawable.ic_file_download_white_24dp)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationHolder notificationHolder = new NotificationHolder(notificationId, contentTitle,
                builder);
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

        return notificationHolder;
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
        NotificationHolder notificationHolder = downloadJobIdToNotificationMap.get((int)downloadJobId);
        if(notificationHolder != null){
            NotificationCompat.Builder builder = notificationHolder.getBuilder();
            builder.setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(contentSubText)
                    .setProgress(MAX_PROGRESS_VALUE,progress,false);
            mNotificationManager.notify(notificationHolder.getNotificationId(),
                    notificationHolder.getBuilder().build());
        }
    }

    /**
     * Update summary notification to show progress as the sum of all download notifications
     */
    private void updateDownloadSummary(){
        NotificationHolder notificationHolder = downloadJobIdToNotificationMap.get(GROUP_SUMMARY_ID);
        if(notificationHolder != null){
            String summaryLabel = impl.getString(MessageID.download_downloading_placeholder,
                    getApplicationContext());
            String title = String.format(summaryLabel,
                    UMFileUtil.INSTANCE.formatFileSize(totalBytesDownloadedSoFar),
                    UMFileUtil.INSTANCE.formatFileSize(totalBytesToBeDownloaded));
            totalBytesDownloadedSoFar = 0L;
            notificationHolder.getBuilder().setSubText(title);
            mNotificationManager.notify(notificationHolder.getNotificationId(),
                    notificationHolder.getBuilder().build());
        }
    }


    /**
     * Stop foreground service
     */
    private void stopForegroundService(){
        if(!stopped.getAndSet(true)) {
            final NetworkManagerAndroidBle networkManager = networkManagerBle;
            if(networkManager != null)
                networkManagerBle.removeDownloadChangeListener(this);

            downloadJobIdToNotificationMap.clear();
            stopForeground(true);
            stopSelf();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mNetworkServiceBound.get())
            unbindService(mNetworkServiceConnection);
    }

    private boolean isVersionLollipopOrAbove(){
       return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private boolean canCreateGroupedNotification(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}

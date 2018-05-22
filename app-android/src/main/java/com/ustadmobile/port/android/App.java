package com.ustadmobile.port.android;

import android.content.Context;

//import com.evernote.android.job.Job;
//import com.evernote.android.job.JobManager;
//import com.evernote.android.job.JobRequest;
import com.toughra.ustadmobile.BuildConfig;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

/**
 * Created by varuna on 8/23/2017.
 *
 * Note: UmBaseApplication extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 *
 */
public class App extends UmBaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.setReportFormat(StringFormat.JSON);

        builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder.class)
                .setUri(BuildConfig.ACRA_HTTP_URI)
                .setBasicAuthLogin(BuildConfig.ACRA_BASIC_LOGIN)
                .setBasicAuthPassword(BuildConfig.ACRA_BASIC_PASS)
                .setHttpMethod(HttpSender.Method.POST).setEnabled(true);

        ACRA.init(this, builder);
    }
}


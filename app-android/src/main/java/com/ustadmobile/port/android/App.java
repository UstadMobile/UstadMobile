package com.ustadmobile.port.android;

import android.app.Application;
import android.content.Context;

import com.ustadmobile.core.db.UmAppDatabase;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import java.io.File;

/**
 * Created by varuna on 8/23/2017.
 *
 * Note: UmBaseApplication extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 *
 */
public class App extends Application {

    public static final String ATTACHMENTS_DIR = "attachments";

    @Override
    public void onCreate() {
        super.onCreate();
        Context appContext = getApplicationContext();
        UmAppDatabase.getInstance(appContext).setAttachmentsDir(new File(appContext.getFilesDir(),
                ATTACHMENTS_DIR).getAbsolutePath());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

//        Temporarily disabled
//        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
//        builder.setReportFormat(StringFormat.JSON);
//
//        builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder.class)
//                .setUri(BuildConfig.ACRA_HTTP_URI)
//                .setBasicAuthLogin(BuildConfig.ACRA_BASIC_LOGIN)
//                .setBasicAuthPassword(BuildConfig.ACRA_BASIC_PASS)
//                .setHttpMethod(HttpSender.Method.POST).setEnabled(true);
//
//        ACRA.init(this, builder);
    }
}


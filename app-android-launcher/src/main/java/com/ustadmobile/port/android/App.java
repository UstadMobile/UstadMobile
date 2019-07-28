package com.ustadmobile.port.android;

import android.content.Context;

import com.toughra.ustadmobile.launcher.BuildConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.generated.MessageIDMap;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

/**
 * Created by varuna on 8/23/2017.
 *
 * Note: UmBaseApplication extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 *
 */
@AcraCore(reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = BuildConfig.ACRA_HTTP_URI,
        basicAuthLogin = BuildConfig.ACRA_BASIC_LOGIN,
        basicAuthPassword = BuildConfig.ACRA_BASIC_PASS,
        httpMethod = HttpSender.Method.POST)
public class App extends UmBaseApplication {

    public static final String ATTACHMENTS_DIR = "attachments";


    @Override
    public void onCreate() {
        super.onCreate();
        UstadMobileSystemImpl.getInstance().setMessageIdMap(MessageIDMap.INSTANCE.getID_MAP());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}


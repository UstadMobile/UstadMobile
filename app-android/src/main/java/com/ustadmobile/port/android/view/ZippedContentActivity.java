package com.ustadmobile.port.android.view;

import android.os.AsyncTask;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;

/**
 * Created by mike on 2/15/18.
 */

public abstract class ZippedContentActivity extends UstadBaseActivity {

    protected static class MountZipAsyncTask extends AsyncTask<String, Void, String> {

        private NetworkManagerAndroid networkManagerAndroid;

        private UmCallback callback;

        protected MountZipAsyncTask(NetworkManagerAndroid networkManagerAndroid, UmCallback callback) {
            this.networkManagerAndroid = networkManagerAndroid;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... strings) {
            String mountedUri = networkManagerAndroid.mountZipOnHttp(strings[0], null, false, null);
            return UMFileUtil.joinPaths(new String[]{networkManagerAndroid.getLocalHttpUrl(),
                    mountedUri});
        }

        @Override
        protected void onPostExecute(String mountedPath) {
            callback.onSuccess(mountedPath);
        }
    }
}

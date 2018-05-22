package com.ustadmobile.port.sharedse.networkmanager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mike on 2/8/18.
 */

public class DefaultURLConnectionOpener implements URLConnectionOpener{

    @Override
    public URLConnection openConnection(URL url) throws IOException{
        return url.openConnection();
    }
}

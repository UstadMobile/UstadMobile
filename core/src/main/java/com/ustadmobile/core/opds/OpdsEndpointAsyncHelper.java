package com.ustadmobile.core.opds;

import java.io.IOException;

/**
 * Created by mike on 12/27/17.
 */

public class OpdsEndpointAsyncHelper {

    public OpdsEndpoint endpoint;

    public OpdsEndpointAsyncHelper(OpdsEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void loadItemAsync(final String opdsUri, final UstadJSOPDSItem item, final Object context,
                              final UstadJSOPDSItem.OpdsItemLoadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    endpoint.loadItem(opdsUri, item, context, callback);
                }catch(IOException e) {
                    callback.onError(item, e);
                }
            }
        }).start();
    }

}

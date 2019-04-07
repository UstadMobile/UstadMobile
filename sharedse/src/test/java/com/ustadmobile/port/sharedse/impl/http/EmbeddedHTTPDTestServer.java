package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.test.core.impl.DodgyInputStream;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class EmbeddedHTTPDTestServer extends EmbeddedHTTPD {

    private AtomicInteger numTimesToFail = new AtomicInteger(0);

    private AtomicInteger requestCount = new AtomicInteger(0);

    private volatile int throttle = 0;

    public EmbeddedHTTPDTestServer(int portNum, Object context, UmAppDatabase appDatabase) {
        super(portNum, context, appDatabase);
    }

    public int getNumTimesToFail() {
        return numTimesToFail.get();
    }

    public void setNumTimesToFail(int numTimesToFail) {
        this.numTimesToFail.set(numTimesToFail);
    }

    @Override
    public Response serve(IHTTPSession session) {
        requestCount.incrementAndGet();
        if(numTimesToFail.get() > 0){
            numTimesToFail.decrementAndGet();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                    null);
        }

        Response r = super.serve(session);
        if(throttle > 0){
            InputStream throttledIn = new DodgyInputStream(r.getData(), throttle, 0);
            r.setData(throttledIn);
        }
        return r;
    }

    public int getRequestCount(){
        return requestCount.get();
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }
}

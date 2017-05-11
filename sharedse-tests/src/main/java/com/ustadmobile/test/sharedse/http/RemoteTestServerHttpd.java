package com.ustadmobile.test.sharedse.http;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kileha3 on 11/05/2017.
 */

public class RemoteTestServerHttpd extends NanoHTTPD {

    public static final String CMD_SETSUPERNODE_ENABLED = "SUPERNODE";

    protected Object context;

    public RemoteTestServerHttpd(int port, Object context) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> decodedParams = decodeParameters(session.getQueryParameterString());
        String command = decodedParams.containsKey("cmd") ? decodedParams.get("cmd").get(0) : null;
        if(CMD_SETSUPERNODE_ENABLED.equals(command)) {
            boolean enabled = Boolean.parseBoolean(decodedParams.get("enabled").get(0));
            UstadMobileSystemImpl.getInstance().getNetworkManager().setSuperNodeEnabled(context, enabled);
        }

        return super.serve(session);
    }
}

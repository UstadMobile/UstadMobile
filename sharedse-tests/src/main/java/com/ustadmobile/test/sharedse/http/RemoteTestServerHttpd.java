package com.ustadmobile.test.sharedse.http;

import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kileha3 on 11/05/2017.
 */

public class RemoteTestServerHttpd extends NanoHTTPD {

    public static final String CMD_SETSUPERNODE_ENABLED = "SUPERNODE";


    protected NetworkManager networkManager;

    public RemoteTestServerHttpd(int port, NetworkManager networkManager) {
        super(port);
        this.networkManager = networkManager;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> decodedParams = decodeParameters(session.getQueryParameterString());
        String command = decodedParams.containsKey("cmd") ? decodedParams.get("cmd").get(0) : null;
        try {
            if(CMD_SETSUPERNODE_ENABLED.equals(command)) {
                boolean enabled = Boolean.parseBoolean(decodedParams.get("enabled").get(0));
                networkManager.setSuperNodeEnabled(networkManager.getContext(), enabled);
                return newFixedLengthResponse("OK");
            }
        }catch(Exception e) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(bout));
            e.printStackTrace(writer);
            writer.flush();
            String exceptionMsg = e.toString()  + "" + new String(bout.toByteArray());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                    exceptionMsg);
        }


        return super.serve(session);
    }
}

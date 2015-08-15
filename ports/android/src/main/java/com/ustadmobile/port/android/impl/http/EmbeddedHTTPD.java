package com.ustadmobile.port.android.impl.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import fi.iki.elonen.*;

/**
 * Created by mike on 8/14/15.
 */
public class EmbeddedHTTPD extends NanoHTTPD {

    private HashMap<String, String> mountedEPUBs;

    public static final String PREFIX_MOUNT = "/mount/";


    public EmbeddedHTTPD(int portNum) {
        super(portNum);
        mountedEPUBs = new HashMap<String, String>();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if(uri.startsWith(PREFIX_MOUNT)) {
            int nextSlash = uri.indexOf('/', PREFIX_MOUNT.length() + 1);

            //check that a sub path is contained; if not return 404
            if(nextSlash == -1) {
                return new  Response(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: only zip is mentioned, not path within:" + uri);
            }
            String zipMountPath = uri.substring(PREFIX_MOUNT.length(), nextSlash);

            if(!mountedEPUBs.containsKey(zipMountPath)) {
                return new Response(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: that zip is not mounted: " + uri);
            }

            String pathInZip = uri.substring(nextSlash+1);

            try {

                ZipFile zipFile = new ZipFile(mountedEPUBs.get(zipMountPath));
                ZipEntry entry = zipFile.getEntry(pathInZip);
                if(entry != null) {
                    return new Response(Response.Status.OK, "text/plain",
                            zipFile.getInputStream(entry));
                }else {
                    return new Response(Response.Status.NOT_FOUND, "text/plain", "Not found within zip: "
                        + pathInZip);
                }
            }catch(IOException e) {
                return new Response(Response.Status.INTERNAL_ERROR, "text/plain", "Exception: "
                    + e.toString());
            }

        }


        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return new Response(msg);
    }

    public void mountZip(String mountPath, String zipPath) {
        mountedEPUBs.put(mountPath, zipPath);
    }




}

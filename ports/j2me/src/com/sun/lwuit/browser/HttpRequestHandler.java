/*
 *  Copyright ? 2008, 2010, Oracle and/or its affiliates. All rights reserved
 */
package com.sun.lwuit.browser;

import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.io.html.AsyncDocumentRequestHandlerImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An implementation of DocumentRequestHandler that handles fetching HTML documents both from HTTP and from the JAR.
 * This request handler takes care of cookies, redirects and handles both GET and POST requests
 *
 * @author Ofir Leitner
 */
public class HttpRequestHandler extends AsyncDocumentRequestHandlerImpl implements DocumentRequestHandler {
    /**
     * A hastable containing all history - the table keys are domain names, while the value is a vector containing the visited links.
     */
    static Hashtable visitedLinks = BrowserStorage.getHistory();


    /**
     * Returns the domain string we use to identify visited link.
     * Note that this may be different than the domain name returned by HttpConnection.getHost
     * 
     * @param url The link URL
     * @return The link's domain
     */
    static String getDomainForLinks(String url) {
        String domain=null;
        if (url.startsWith("jar:")) {
            return "localhost"; // Just a common name to store local files under
        }
        if (url.startsWith("file:")) {
            return "localhost"; // Just a common name to store local files under
        } 
        int index=-1;
        if (url.startsWith("http://")) {
            index=7;
        } else if (url.startsWith("https://")) {
            index=8;
        }
        if (index!=-1) {
            domain=url.substring(index);
            index=domain.indexOf('/');
            if (index!=-1) {
                domain=domain.substring(0,index);
            }
        }
        return domain;
    }

    /**
     * {@inheritDoc}
     */
    public void resourceRequestedAsync(final DocumentInfo docInfo, final IOCallback callback) {
        String url=docInfo.getUrl();

        String linkDomain=getDomainForLinks(url);

        // Visited links
        if (docInfo.getExpectedContentType()==DocumentInfo.TYPE_HTML) { // Only mark base documents as visited links
            
            if (linkDomain!=null) {
                Vector hostVisitedLinks=(Vector)visitedLinks.get(linkDomain);
                if (hostVisitedLinks==null) {
                    hostVisitedLinks=new Vector();
                    visitedLinks.put(linkDomain,hostVisitedLinks);
                }
                if (!hostVisitedLinks.contains(url)) {
                    hostVisitedLinks.addElement(url);
                    BrowserStorage.addHistory(linkDomain, url);
                }
            } else {
                System.out.println("Link domain null for "+url);
            }
        } 

        super.resourceRequestedAsync(docInfo, callback);
    }
}


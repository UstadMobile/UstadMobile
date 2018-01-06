package com.ustadmobile.core.scorm;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by mike on 1/6/18.
 */

public class ScormManifest {

    public static class Organization {
        List<OrganizationItem> items;

    }

    public static class OrganizationItem {
        String title;

        String identifierRef;
    }

    public static class Resource {
        String identifier;

        String iType;

        String scormType;

        String href;
    }

    private Map<String, String> title;

    private String identifier;

    private String defaultOrganizationIdentifier;

    private Map<String, Organization> organizationsMap;

    private Map<String, Resource> resourceMap;

    public static final String NS_IMS = "http://www.imsproject.org/xsd/imscp_rootv1p1p2";

    public static final String ATTR_IDENTIFIER = "identifier";

    public static final String TAG_MANIFEST = "manifest";


    public ScormManifest() {
        organizationsMap = new HashMap<>();
        resourceMap = new HashMap<>();
    }

    public void loadFromXpp(XmlPullParser xpp) throws IOException, XmlPullParserException{
        int evtType;
        String tagName;

        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            switch(evtType) {
                case XmlPullParser.START_TAG:
                    tagName = xpp.getName();

                    if(TAG_MANIFEST.equals(tagName)) {
                        identifier = xpp.getAttributeValue(NS_IMS, ATTR_IDENTIFIER);
                    }

                    break;
            }
        }
    }

    /**
     *
     * @param in
     * @throws IOException
     */
    public void loadFromInputStream(InputStream in) throws IOException, XmlPullParserException{
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser();
        xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        xpp.setInput(in, "UTF-8");
        loadFromXpp(xpp);
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

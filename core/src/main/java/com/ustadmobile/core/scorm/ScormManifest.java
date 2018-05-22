package com.ustadmobile.core.scorm;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a scorm manifest.
 *
 * Created by mike on 1/6/18.
 */
public class ScormManifest {

    public static class Organization {

        String identifier;

        String title;

        List<OrganizationItem> items;

        public Organization() {
            items = new ArrayList<>();
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getTitle() {
            return title;
        }

        public List<OrganizationItem> getItems() {
            return items;
        }
    }

    public static class OrganizationItem {
        String title;

        String identifierRef;

        String identifier;

        public String getIdentifier() {
            return identifier;
        }

        public String getTitle() {
            return title;
        }

        public String getIdentifierRef() {
            return identifierRef;
        }
    }

    public static class Resource {
        String identifier;

        String iType;

        String scormType;

        String href;

        public String getIdentifier() {
            return identifier;
        }

        public String getiType() {
            return iType;
        }

        public String getScormType() {
            return scormType;
        }

        public String getHref() {
            return href;
        }
    }

    private Map<String, String> title;

    private String identifier;

    private String defaultOrganizationIdentifier;

    private Map<String, Organization> organizationsMap;

    private Map<String, Resource> resourceMap;

    public static final String NS_IMS = "http://www.imsproject.org/xsd/imscp_rootv1p1p2";

    public static final String NS_ADLCP = "http://www.adlnet.org/xsd/adlcp_rootv1p2";

    public static final String ATTR_IDENTIFIER = "identifier";

    public static final String ATTR_IDENTIFIERREF = "identifierref";

    public static final String ATTR_DEFAULT = "default";

    public static final String ATTR_HREF = "href";

    public static final String ATTR_TYPE = "type";

    public static final String ATTR_SCORMTYPE = "scormtype";

    public static final String TAG_MANIFEST = "manifest";

    public static final String TAG_ORGANIZATIONS = "organizations";

    public static final String TAG_ORGANIZATION = "organization";

    public static final String TAG_ITEM = "item";

    public static final String TAG_TITLE = "title";

    public static final String TAG_RESOURCE = "resource";


    public ScormManifest() {
        organizationsMap = new HashMap<>();
        resourceMap = new HashMap<>();
    }

    public void loadFromXpp(XmlPullParser xpp) throws IOException, XmlPullParserException{
        int evtType;
        String tagName;

        Organization currentOrg = null;
        OrganizationItem currentOrgItem = null;

        Resource currentResource;

        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            switch(evtType) {
                case XmlPullParser.START_TAG:
                    tagName = xpp.getName();

                    if(TAG_MANIFEST.equals(tagName)) {
                        identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER);
                    }else if(TAG_ORGANIZATIONS.equals(tagName)) {
                        defaultOrganizationIdentifier = xpp.getAttributeValue(null, ATTR_DEFAULT);
                    }else if(TAG_ORGANIZATION.equals(tagName)) {
                        currentOrg = new Organization();
                        currentOrg.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER);
                        organizationsMap.put(currentOrg.identifier, currentOrg);
                    }else if(currentOrg != null && TAG_ITEM.equals(tagName)) {
                        currentOrgItem = new OrganizationItem();
                        currentOrgItem.identifierRef = xpp.getAttributeValue(null, ATTR_IDENTIFIERREF);
                        currentOrgItem.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER);
                        currentOrg.items.add(currentOrgItem);
                    }else if(currentOrgItem != null && TAG_TITLE.equals(tagName)
                            && xpp.next() == XmlPullParser.TEXT) {
                        currentOrgItem.title = xpp.getText();
                    }else if(currentOrg != null && TAG_TITLE.equals(tagName)
                        && xpp.next() == XmlPullParser.TEXT) {
                        currentOrg.title = xpp.getText();
                    }else if(TAG_RESOURCE.equals(tagName)) {
                        currentResource = new Resource();
                        currentResource.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER);
                        currentResource.iType = xpp.getAttributeValue(null, ATTR_TYPE);
                        currentResource.href = xpp.getAttributeValue(null, ATTR_HREF);
                        currentResource.scormType = xpp.getAttributeValue(NS_ADLCP, ATTR_SCORMTYPE);
                        resourceMap.put(currentResource.identifier, currentResource);
                    }

                    break;

                case XmlPullParser.END_TAG:
                    tagName = xpp.getName();

                    if(TAG_ORGANIZATION.equals(tagName)) {
                        currentOrg = null;
                    }else if(TAG_ITEM.equals(tagName)) {
                        currentOrgItem = null;
                    }else if(TAG_RESOURCE.equals(tagName)){
                        currentResource = null;
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

    public Organization getOrganizationByIdentifier(String identifier){
        return organizationsMap.get(identifier);
    }

    public String getDefaultOrganizationIdentifier() {
        return defaultOrganizationIdentifier;
    }

    public Resource getResourceByIdentifier(String identifier) {
        return resourceMap.get(identifier);
    }

    public Organization getDefaultOrganization() {
        return getOrganizationByIdentifier(defaultOrganizationIdentifier);
    }
}

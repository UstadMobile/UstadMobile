package com.ustadmobile.core.catalog;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;

import java.util.Vector;

/**
 * This is a utility class that helps manage support for different content types. It will cache
 * instantiated instances of ContentTypePlugin classes and retrieve the correct view name for any
 * given content item.
 *
 * Created by mike on 9/13/17.
 */
public class ContentTypeManager {

    private static ContentTypePlugin[] supportedPlugins;

    /**
     * The ContentTypePlugins available based on what is returned by
     * UstadMobileSystemImpl.getSupoprtedContentTypePlugins. This returns a list of classes
     * representing a ContentTypePlugin. This method will instantiate each of those classes and
     * cache the results.
     *
     * @see UstadMobileSystemImpl#getSupportedContentTypePlugins()
     * @return Array of all supported ContentTypePlugins available on the system
     */
    public static ContentTypePlugin[] getSupportedContentTypePlugins() {
        if(supportedPlugins != null)
            return supportedPlugins;

        Class[] supportedContentTypes = UstadMobileSystemImpl.getInstance()
                .getSupportedContentTypePlugins();
        Vector instantiatedPlugins = new Vector();

        for(int i = 0; i < supportedContentTypes.length; i++) {
            try {
                instantiatedPlugins.addElement(supportedContentTypes[i].newInstance());
            }catch(InstantiationException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 670, supportedContentTypes[i].getName());
            }catch(IllegalAccessException a) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 671, supportedContentTypes[i].getName());
            }
        }

        supportedPlugins = new ContentTypePlugin[instantiatedPlugins.size()];
        instantiatedPlugins.toArray(supportedPlugins);
        return supportedPlugins;
    }

    /**
     * Find the appropriate view name for the given mime type (for use with UstadMobileSystemImpl.go)
     *
     * @see UstadMobileSystemImpl#go(String, Object)
     *
     * @param mime Mime type of the content
     * @return The view name for this type of content, or null if no plugin supports this mime type
     */
    public static String getViewNameForContentType(String mime) {
        ContentTypePlugin[] plugins = getSupportedContentTypePlugins();
        int supportedMimeIndex;
        for(int i = 0; i < plugins.length; i++) {
            supportedMimeIndex = UMUtil.indexInArray(plugins[i].getMimeTypes(), mime);
            if(supportedMimeIndex != -1)
                return plugins[i].getViewName();
        }

        return null;
    }

}

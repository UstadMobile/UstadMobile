package com.ustadmobile.core.catalog;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.util.UMUtil;

/**
 * This is a utility class that helps manage support for different content types. It will cache
 * instantiated instances of ContentTypePlugin classes and retrieve the correct view name for any
 * given content item.
 *
 * Created by mike on 9/13/17.
 */
public class ContentTypeManager {

    /**
     * Find the appropriate view name for the given mime type (for use with UstadMobileSystemImpl.go)
     *
     * @see UstadMobileSystemImpl#go(String, Object)
     *
     * @param mime Mime type of the content
     * @return The view name for this type of content, or null if no plugin supports this mime type
     */
    public static String getViewNameForContentType(String mime) {
        ContentTypePlugin[] plugins = UstadMobileSystemImpl.getInstance().getSupportedContentTypePlugins();

        for(int i = 0; i < plugins.length; i++) {
            if(plugins[i].getMimeTypes().contains(mime))
                return plugins[i].getViewName();

        }

        return null;
    }

}

package com.ustadmobile.core.contentformats.scorm

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.io.InputStream
import org.kmp.io.KMPPullParser
import org.kmp.io.KMPXmlParser

/**
 * Represents a scorm manifest.
 *
 * Created by mike on 1/6/18.
 */
class ScormManifest {

    var title: Map<String, String>? = null

    var identifier: String? = null

    var defaultOrganizationIdentifier: String? = null
        private set

    private val organizationsMap: MutableMap<String, Organization>

    private val resourceMap: MutableMap<String, Resource>

    val defaultOrganization: Organization
        get() = getOrganizationByIdentifier(defaultOrganizationIdentifier)

    class Organization {

        var identifier: String? = null
            internal set

        var title: String? = null
            internal set

        internal var items: MutableList<OrganizationItem> = ArrayList()

        fun getItems(): List<OrganizationItem> {
            return items
        }
    }

    class OrganizationItem {
        var title: String? = null
            internal set

        var identifierRef: String? = null
            internal set

        var identifier: String? = null
            internal set
    }

    class Resource {
        var identifier: String? = null
            internal set

        internal var iType: String? = null

        var scormType: String? = null
            internal set

        var href: String? = null
            internal set

        fun getiType(): String? {
            return iType
        }
    }

    init {
        organizationsMap = HashMap()
        resourceMap = HashMap()
    }

    fun loadFromXpp(xpp: KMPXmlParser) {
        var evtType: Int ? = null
        var tagName: String

        var currentOrg: Organization? = null
        var currentOrgItem: OrganizationItem? = null

        var currentResource: Resource?

        while ({evtType = xpp.next(); evtType}() != KMPPullParser.END_DOCUMENT) {
            when (evtType) {
                KMPPullParser.START_TAG -> {
                    tagName = xpp.getName()?: ""

                    if (TAG_MANIFEST == tagName) {
                        identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER)
                    } else if (TAG_ORGANIZATIONS == tagName) {
                        defaultOrganizationIdentifier = xpp.getAttributeValue(null, ATTR_DEFAULT)
                    } else if (TAG_ORGANIZATION == tagName) {
                        currentOrg = Organization()
                        currentOrg.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER)
                        organizationsMap[currentOrg.identifier!!] = currentOrg
                    } else if (currentOrg != null && TAG_ITEM == tagName) {
                        currentOrgItem = OrganizationItem()
                        currentOrgItem.identifierRef = xpp.getAttributeValue(null, ATTR_IDENTIFIERREF)
                        currentOrgItem.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER)
                        currentOrg.items.add(currentOrgItem)
                    } else if (currentOrgItem != null && TAG_TITLE == tagName
                            && xpp.next() == KMPPullParser.TEXT) {
                        currentOrgItem.title = xpp.getText()
                    } else if (currentOrg != null && TAG_TITLE == tagName
                            && xpp.next() == KMPPullParser.TEXT) {
                        currentOrg.title = xpp.getText()
                    } else if (TAG_RESOURCE == tagName) {
                        currentResource = Resource()
                        currentResource.identifier = xpp.getAttributeValue(null, ATTR_IDENTIFIER)
                        currentResource.iType = xpp.getAttributeValue(null, ATTR_TYPE)
                        currentResource.href = xpp.getAttributeValue(null, ATTR_HREF)
                        currentResource.scormType = xpp.getAttributeValue(NS_ADLCP, ATTR_SCORMTYPE)
                        resourceMap[currentResource.identifier!!] = currentResource
                    }
                }

                KMPPullParser.END_TAG -> {
                    tagName = xpp.getName()?: ""

                    when {
                        TAG_ORGANIZATION == tagName -> currentOrg = null
                        TAG_ITEM == tagName -> currentOrgItem = null
                        TAG_RESOURCE == tagName -> currentResource = null
                    }
                }
            }
        }
    }

    /**
     *
     * @param in
     */
    fun loadFromInputStream(`in`: InputStream) {
        val xpp = UstadMobileSystemImpl.instance.newPullParser()
        xpp.setFeature(KMPPullParser.FEATURE_PROCESS_NAMESPACES, true)
        xpp.setInput(`in`, "UTF-8")
        loadFromXpp(xpp)
    }

    fun getOrganizationByIdentifier(identifier: String?): Organization {
        return organizationsMap.get(identifier)!!
    }

    fun getResourceByIdentifier(identifier: String): Resource {
        return resourceMap[identifier]!!
    }

    companion object {

        val NS_IMS = "http://www.imsproject.org/xsd/imscp_rootv1p1p2"

        val NS_ADLCP = "http://www.adlnet.org/xsd/adlcp_rootv1p2"

        val ATTR_IDENTIFIER = "identifier"

        val ATTR_IDENTIFIERREF = "identifierref"

        val ATTR_DEFAULT = "default"

        val ATTR_HREF = "href"

        val ATTR_TYPE = "type"

        val ATTR_SCORMTYPE = "scormtype"

        val TAG_MANIFEST = "manifest"

        val TAG_ORGANIZATIONS = "organizations"

        val TAG_ORGANIZATION = "organization"

        val TAG_ITEM = "item"

        val TAG_TITLE = "title"

        val TAG_RESOURCE = "resource"
    }
}

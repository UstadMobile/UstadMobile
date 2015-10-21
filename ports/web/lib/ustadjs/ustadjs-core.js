/*

UstadJS

Copyright 2014 UstadMobile, Inc
  www.ustadmobile.com

Ustad Mobile is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version with the following additional terms:
 
All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
LLC must be kept as they are in the original distribution.  If any new
screens are added you must include the Ustad Mobile logo as it has been
used in the original distribution.  You may not create any new
functionality whose purpose is to diminish or remove the Ustad Mobile
Logo.  You must leave the Ustad Mobile logo as the logo for the
application to be used with any launcher (e.g. the mobile app launcher).
 
If you want a commercial license to remove the above restriction you must
contact us and purchase a license without these restrictions.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
Ustad Mobile is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */
var UstadJS;

UstadJS = {
    
    /**
     * Make sure that the given src (which may be an xml doc or string) is returned
     * as an xml doc.  If it's a string, parse it into XML
     * 
     * @param {Object} src String or XMLDocument
     * @returns {Document} XML document of the given src
     */
    ensureXML: function(src) {
        if(typeof src === "string") {
            var parser = new DOMParser();
            src = parser.parseFromString(src, "text/xml");
        }
        
        return src;
    },
    
    /**
     * Get a JSON list of 
     * 
     * @param src {Object} XML string or XMLDocument object.  Will be parsed if String
     * 
     * @returns {Array} Array of JSON Objects for each rootfile in manifest
     * each with full-path and media-type attributes
     */
    getContainerRootfilesFromXML: function(src) {
        src = UstadJS.ensureXML(src);
        var retVal = [];
        var rootFileNodes = src.getElementsByTagName("rootfile");
        for(var i = 0; i < rootFileNodes.length; i++) {
            var currentChild = {
                "full-path" : rootFileNodes[i].getAttribute("full-path"),
                "media-type" : rootFileNodes[i].getAttribute("media-type")
            };
            retVal.push(currentChild);
        }
        
        return retVal;
    },
    
    /**
     * Handle running an optional calback with specified context and args
     * 
     * @param {function} fn function to run
     * @param {Object} context Context for this object
     * @param {Array} args to pass
     * @returns {undefined}
     */
    runCallback: function(fn, context, args) {
        if(typeof fn !== "undefined" && fn !== null) {
            fn.apply(context, args);
        }   
    },
    
    /**
     * Remove the query portion of a URL (if present)
     * 
     * @param fullURL {String} URL possibly including a query string
     * 
     * @returns {String} the URL without the query string if it was there...
     */
    removeQueryFromURL: function(fullURL) {
        if(fullURL.indexOf("?") !== -1) {
            return fullURL.substring(0, fullURL.indexOf("?"));
        }else {
            return fullURL;
        }
    },
    
    /**
     * From the given url, which may be relative or absolute; construct an 
     * absoulte url.  Will use location object to determine current base URL
     * 
     * @method
     * 
     * @param {String} url Given url which may be absolute (e.g. starts with http:// or https://) or relative
     * 
     * @returns {String} URL made absoulute if it was not before
     */
    makeAbsoluteURL: function(url) {
        return UstadJS.resolveURL(location.href, url);
    }
};

UstadJS.getDirPath = function(completePath) {
    //in case the last character is a trailing slash - assume this
    if(completePath.lastIndexOf("/") === completePath.length-1) {
        return completePath;
    }
    
    return completePath.substring(0, completePath.lastIndexOf("/"));
};

/**
 * Basic interpretation of name=val query parameters - will decode both the
 * parameter name and the parameter value.  There must be a ? in the string
 * e.g. as location.query returns ?foo=bar ...
 * 
 * @param {String} queryStr An string with HTTP Query variables after a ?
 * @returns {Object} 
 */
UstadJS.getURLQueryVars = function(queryStr) {
    var queryVars = {};
    var qPos = queryStr.indexOf('?');
    if(qPos !== -1 && queryStr.length > qPos+2) {
        var query = queryStr.substring(qPos+1);
        var vars = query.split("&");
        for (var i=0;i<vars.length;i++) {
            var pair = vars[i].split("=");
            var paramName = decodeURIComponent(pair[0]);
            var paramVal = decodeURIComponent(pair[1]);
            queryVars[paramName] = paramVal;
        }
    }
    
    return queryVars;
};

UstadJS.resolveURL = function(baseURL, linkedURL) {
    var linkedURLLower = linkedURL.toLowerCase();
    var linkedURLWithoutQuery = linkedURL.indexOf("?") !== -1 ? 
        linkedURLLower.substring(0, linkedURL.indexOf("?")) :
        linkedURLLower;
    if(linkedURLWithoutQuery.match(/[a-zA-Z0-9]+:\/\//)) {
        return linkedURL;//this is absolute
    }
    
    if(linkedURL.substring(0, 2) === "//") {
        //this means match the protocol only
        var resolvedURL = baseURL.substring(0, baseURL.indexOf(":")+1) +
            linkedURL;
        return resolvedURL;
    }
    
    if(linkedURL.charAt(0) === "/") {
        var serverStart = baseURL.indexOf("://")+3;
        var serverFinish = baseURL.indexOf("/", serverStart);
        return baseURL.substring(0, serverFinish) + linkedURL;
    }
    
    
    var baseURLPath = UstadJS.getDirPath(UstadJS.removeQueryFromURL(baseURL));
    
    //get rid of trailing / - will be joined with a /
    baseURLPath = baseURLPath.lastIndexOf("/") === baseURLPath.length - 1 ? 
        baseURLPath.substring(0, baseURL.length-1) : baseURLPath;
    var baseParts = baseURLPath.split("/");
    var linkParts = linkedURL.split("/");
    
    for(var i = 0; i < linkParts.length; i++) {
        if(linkParts[i] === '.') {
            continue;
        }
        
        if(linkParts[i] === '..') {
            baseParts.pop();
        }else {
            baseParts.push(linkParts[i]);
        }
    }
    
    return baseParts.join("/");
    
};

var UstadJSOPDSFeed = null;

UstadJSOPDSFeed = function(title, id) {
    this.entries = [];
    this.xmlDoc = document.implementation.createDocument(
            "http://www.w3.org/2005/Atom", "feed");
        
    var idEl = this.xmlDoc.createElementNS("http://www.w3.org/2005/Atom", "id");
    idEl.textContent = id;
    this.xmlDoc.documentElement.appendChild(idEl);
    
    var titleEl = this.xmlDoc.createElementNS("http://www.w3.org/2005/Atom", 
        "title");
    titleEl.textContent = title;
    this.xmlDoc.documentElement.appendChild(titleEl);
    
    this.href = null;
};

/**
 * From the given source document make an object representing an OPDS feed
 * 
 * @param {Object} xmlSrc String or XML Document with the opds feed
 * @param {String} href URL of the opdsSrc
 * @returns {UstadJSOPDSFeed}
 */
UstadJSOPDSFeed.loadFromXML = function(xmlSrc, href) {
    xmlSrc = UstadJS.ensureXML(xmlSrc);
    var opdsFeedObj = new UstadJSOPDSFeed();
    opdsFeedObj.xmlDoc = xmlSrc;
    opdsFeedObj.href = href;
    
    //set properties up
    opdsFeedObj.setPropsFromXML(xmlSrc);
    
    return opdsFeedObj;
};


UstadJSOPDSFeed.prototype = {
    
    setPropsFromXML: function(xmlSrc) {
        xmlSrc = xmlSrc ? xmlSrc : this.xmlDoc;
        
        this.title = xmlSrc.querySelector("feed > title").textContent;
        this.id = xmlSrc.querySelector("feed > id").textContent;
        
        //now go through all entries
        var entryNodes = xmlSrc.getElementsByTagNameNS(
                "http://www.w3.org/2005/Atom", "entry");
        
        this.entries = [];
        
        for(var i = 0; i < entryNodes.length; i++) {
            var newEntry = new UstadJSOPDSEntry(entryNodes[i], this);
            this.entries.push(newEntry);
        }
    },
    
    
    /**
     * Returns true if this is an acquisition OPDS feed; false otherwise.
     * As per the OPDS spec an acquisition feed is any feed that contains
     * acquisition links on entries
     * 
     * @returns {boolean} whether this an acquisition feed or not
     */
    isAcquisitionFeed: function() {
        var result = this.getEntriesByLinkParams(
            UstadJSOPDSEntry.LINK_ACQUIRE, null, 
            {linkRelByPrefix: true});
        return (result.length > 0);
    },
    
    /**
     * Add an opds entry to this feed
     * 
     * @param {UstadJSOPDSEntry} opdsEntry the entry to add
     */
    addEntry: function(opdsEntry) {
        this.entries.push(opdsEntry);
        var entryNode = opdsEntry.xmlNode;
        if(entryNode.ownerDocument !== this.xmlDoc) {
            entryNode = this.xmlDoc.importNode(opdsEntry.xmlNode, true);            
        }
        
        //In case links come from another feed: convert to being absolute href links
        if(opdsEntry.parentFeed !== this) {
            var linkEls = entryNode.getElementsByTagNameNS(
                "http://www.w3.org/2005/Atom", "link");
            for(var i = 0; i < linkEls.length; i++) {
                var linkHREFAbs = UstadJS.resolveURL(opdsEntry.parentFeed.href,
                    linkEls[i].getAttribute("href"));
                linkEls[i].setAttribute("href", linkHREFAbs);
            }
        }

        this.xmlDoc.documentElement.appendChild(entryNode);
        entryNode.namespaceURI = "http://www.w3.org/2005/Atom";
    },
    
    /**
     * Get the entries in this feed according to the link type being looked
     * for e.g. get entries that are navigation catalogs by using:
     * 
     * getEntriesByLinkType("application/atom+xml;profile=opds-catalog;kind=acquisition")
     * 
     * 
     
     * @param {string} linkRel relationship of link to look for e.g. 
     * http://opds-spec.org/acquisition .  Null will match all types and those 
     * without type attribute
     * 
     * @param {string} linkType mime type of link to look for
     * 
     * @param {boolean} [options.linkRelByPrefix=false] get matches if the
     * given linkRel matches the start of that on the element e.g.
     * http://opds-spec.org/acquisition would also match a link with rel
     * http://opds-spec.org/acquisition/open-access
     * 
     * @param {boolean} [options.mimeTypeByPrefix=false] get matches if the given
     * mimeType of the link matches the start of the mimeType argument e.g.
     * application/epub would match application/epub+zip
          * 
     * @returns {UstadJSOPDSEntry[]}
     */
    getEntriesByLinkParams: function(linkRel, linkType, options) {
        var matchingEntries = [];
        
        for(var i = 0; i < this.entries.length; i++) {
            var linksByType = this.entries[i].getLinks(linkRel, linkType, 
                options);
            if(linksByType.length > 0) {
                matchingEntries.push(this.entries[i]);
            }
        }
        
        return matchingEntries;
    },
    
    /**
     * Serialize this opds feed as an XML string
     * 
     * @returns {string} XML of this feed as a string
     */
    toString: function() {
        return new XMLSerializer().serializeToString(this.xmlDoc);
    },
    
    /**
     * Find a UstadJSOPDSEntry object in the feed according to the id
     * 
     * @param {string} entryId to look for
     * 
     * @return {UstadJSOPDSEntry} object representing the requested feed entry, null if not found
     */
    getEntryById: function(entryId) {
        for(var i = 0; i < this.entries.length; i++) {
            if(this.entries[i].id === entryId) {
                return this.entries[i];
            }
        }
        
        return null;
    }
    
};


var UstadJSOPDSEntry = null;

UstadJSOPDSEntry = function(xmlNode, parentFeed) {
    this.xmlNode = null;
    this.parentFeed = null;
    this.title = null;
    this.id = null;
    
    if(parentFeed) {
        this.parentFeed = parentFeed;
    }
    
    if(xmlNode) {
        this.loadFromXMLNode(xmlNode);
    }
};

/**
 * OPDS constant for the standard acquisition link
 * @type String
 */
UstadJSOPDSEntry.LINK_ACQUIRE = "http://opds-spec.org/acquisition";
    
/**
 * OPDS constant for open access acquisition link
 * @type String
 */
UstadJSOPDSEntry.LINK_ACQUIRE_OPENACCESS = 
        "http://opds-spec.org/acquisition/open-access";

/**
 * OPDS constant for the cover image / artwork for an item
 * @type Strnig
 */
UstadJSOPDSEntry.LINK_IMAGE = "http://opds-spec.org/image";

/**
 * OPDS constnat for the thumbnail
 * @type String
 */
UstadJSOPDSEntry.LINK_THUMBNAIL = "http://opds-spec.org/image/thumbnail";

/**
 * Atom/XML feed mime type constant
 * 
 * @type String
 */
UstadJSOPDSEntry.TYPE_ATOMFEED = "application/atom+xml";

/**
 * Type to be used for a catalog link of an acquisition feed as per OPDS spec
 * 
 * @type String
 */
UstadJSOPDSEntry.TYPE_ACQUISITIONFEED = 
        "application/atom+xml;profile=opds-catalog;kind=acquisition";


/**
 * Type to be used for a navigation feed as per OPDS spec
 * 
 * @type String
 */
UstadJSOPDSEntry.TYPE_NAVIGATIONFEED =
        "application/atom+xml;profile=opds-catalog;kind=navigation";

/**
 * The type of link used for an epub file itself
 * 
 * @type String
 */
UstadJSOPDSEntry.TYPE_EPUBCONTAINER = "application/epub+zip";

UstadJSOPDSEntry.prototype = {
    
    
    /**
     * Get the aquisition links by 
     * @param {String} linkRel - the link relation desired - e.g. 
     *  http://opds-spec.org/acquisition/open-access or 
     *  http://opds-spec.org/acquisition/
     *  Can be null to match any
     *  
     * @param {String} mimeType the desired content mimetype e.g. application/epub+zip
     *  Can be null to match any
     * 
     * @param {boolean} fallback if the desired acquisition type is not available,
     *  should we return plain old http://opds-spec.org/acquisition
     *  
     *  @deprecated this is incorrectly named and should be returning an array not a single href value
     *  
     * @returns {String} the href of the selected link, null if nothing found
     */
    getAcquisitionLinks: function(linkRel, mimeType, fallback) {
        var getLinkArgs = fallback ? { linkRelByPrefix : true } : {};
        var getLinkResult = this.getLinks(linkRel, mimeType, getLinkArgs);
        if(getLinkResult.length === 0) {
            return null;
        }else {
            return getLinkResult[0].href;
        }
    },
    
    /**
     * Get the first available acquisition links href - useful shortcut function
     * when looking a link for an item that would have only one acquisition link
     * 
     * @returns {String} href of the first acquisition link in entry, null if none available
     */
    getFirstAcquisitionLink: function() {
        var getLinkResult = this.getLinks(UstadJSOPDSEntry.LINK_ACQUIRE, null,
            {linkRelByPrefix: true});
        if(getLinkResult.length === 0) {
            return null;
        }else {
            return getLinkResult[0].href;
        }
    },
    
    /**
     * Gets the navigation link for this feed.  This is designed to work for
     * entries in a navigation feed where the link is another atom/xml feed
     * 
     * @returns {Object} 
     */
    getNavigationLink: function() {
        var linkResults = this.getLinks(null, UstadJSOPDSEntry.TYPE_ATOMFEED,
            {mimeTypeByPrefix: true});
        if(linkResults.length > 0) {
            return linkResults[0];
        }else {
            return null;
        }
    },
    
    
    /**
     * Get links for this entry according to the relation or mimeType
     * 
     * @param {string} linkRel e.g. UstadJSOPDSEntry.LINK_ACQUIRE
     * 
     * @param {string} mimeType e.g. UstadJSOPDSEntry.TYPE_ACQUISITIONFEED 
     * or UstadJSOPDSEntry.TYPE_EPUBCONTAINER
     * 
     * @param {Object} options misc options
     * @param {boolean} [options.linkRelByPrefix=false] get matches if the
     * given linkRel matches the start of that on the element e.g.
     * http://opds-spec.org/acquisition would also match a link with rel
     * http://opds-spec.org/acquisition/open-access
     * 
     * @param {boolean} [options.mimeTypeByPrefix=false] get matches if the given
     * mimeType of the link matches the start of the mimeType argument e.g.
     * application/epub would match application/epub+zip
     * 
     * @returns {Array<Object>} object with href, rel and type properties
     */
    getLinks: function(linkRel, mimeType, options) {
        var result = [];
        var linkElements = this.xmlNode.getElementsByTagNameNS(
            "http://www.w3.org/2005/Atom", "link");
        options = options || {};
    
        for(var i = 0; i < linkElements.length; i++) {
            var matchRel = true;
            var matchType = true;
            
            if(linkRel !== null) {
                var linkRelCompare = options.linkRelByPrefix ? 
                    linkElements[i].getAttribute("rel").substring(0, linkRel.length) :
                    linkElements[i].getAttribute("rel");
                matchRel = (linkRel === linkRelCompare);
            }
            
            if(mimeType !== null) {
                var mimeTypeCompare = options.mimeTypeByPrefix ?
                    linkElements[i].getAttribute("type").substring(0, mimeType.length) :
                    linkElements[i].getAttribute("type");
                matchType = (mimeTypeCompare === mimeType);
            }
            
            if(matchRel && matchType) {
                result.push({
                    href : linkElements[i].getAttribute("href"),
                    type : linkElements[i].getAttribute("type"),
                    rel : linkElements[i].getAttribute("rel")
                });
            }
        }
        
        return result;
    },
    
    /**
     * Return a URL to the thumbnail of this item.  Will look first for
     * the thumbnail link, if that is not found; then will return the main 
     * image.
     * 
     * @returns {String} URL of thumbnail if found, null otherwise
     */
    getThumbnail: function(options) {
        var thumbnailLinks = this.getLinks(UstadJSOPDSEntry.LINK_THUMBNAIL, null);
        if(thumbnailLinks.length > 0) {
            return thumbnailLinks[0].href;
        }
        
        var imageLinks = this.getLinks(UstadJSOPDSEntry.LINK_IMAGE, null);
        if(imageLinks.length > 0) {
            return imageLinks[0].href;
        }
        
        return null;
    },
    
    /**
     * Gets the publisher if specified for this entry, null if not present
     * 
     * @returns {String} 
     */
    getPublisher: function() {
        var publisherElements = this.xmlNode.getElementsByTagNameNS(
            "http://purl.org/dc/terms/", "publisher");
        if(publisherElements.length > 0) {
            return publisherElements[0].textContent;
        }else {
            return null;
        }
    },
    
    loadFromXMLNode: function(xmlNode) {
        this.xmlNode = xmlNode;
        this.title = xmlNode.querySelector("title").textContent;
        this.id = xmlNode.querySelector("id").textContent;
        
        
    },
    
    /**
     * Make entry given compulsary requirements for an atom entry
     * 
     * @param {Object} item
     * @param {string} item.title title of the entry contained
     * @param {string} item.id id of the entry contained
     * 
     */
    setupEntry: function(item) {
        var parentDoc = this.parentFeed.xmlDoc;
        this.xmlNode = parentDoc.createElementNS(
            "http://www.w3.org/2005/Atom", "entry");
        var titleEl = parentDoc.createElementNS(
            "http://www.w3.org/2005/Atom", "title");
        titleEl.textContent = item.title;
        this.xmlNode.appendChild(titleEl);
        this.title = item.title;
        
        var idEl = parentDoc.createElementNS(
                "http://www.w3.org/2005/Atom", "id");
        idEl.textContent = item.id;
        this.xmlNode.appendChild(idEl);
        this.id = item.id;
        
        this.parentFeed.addEntry(this);
        
    },
    
    /**
     * Summary will come from atom:summary link if present and NOT dc:description
     * etc as per the OPDS spec (section 8.1).  If neither are present then
     * we will return teh fallbackVal if specified
     * 
     * @param {string} [fallbackVal=null] optional fallback value to use in case 
     * nothing found
     * 
     * @returns Content of atom:summary for entry, if absent atom:content, 
     * otherwise fallbackVal
     */
    getSummary: function(fallbackVal) {
        var summaryEls = this.xmlNode.getElementsByTagNameNS(
            "http://www.w3.org/2005/Atom", "summary");
        if(summaryEls.length > 0) {
            return summaryEls[0].textContent;
        }
        
        var descEls = this.xmlNode.getElementsByTagNameNS(
            "http://www.w3.org/2005/Atom", "content");
        if(descEls.length > 0) {
            return descEls[0].textContent;
        }
        
        fallbackVal = (typeof fallbackVal !== "undefined") ? fallbackVal : null;
        return fallbackVal;
    },
    
    /**
     * Add a link to this entry
     * 
     * @param {string} rel Relationship e.g. subsection or http://opds-spec.org/acquisition
     * @param {string} href href to link
     * @param {string} mimeType Mime type of item being linked to
     * @returns {undefined}
     */
    addLink: function(rel, href, mimeType) {
        var parentDoc = this.parentFeed.xmlDoc;
        var linkNode = parentDoc.createElementNS(
            "http://www.w3.org/2005/Atom", "link");
        linkNode.setAttribute("rel", rel);
        linkNode.setAttribute("href", href);
        linkNode.setAttribute("type", mimeType);
        
        this.xmlNode.appendChild(linkNode);
    }
    
    
};



/*
var UstadJSContainer = null;

UstadJSContainer = function() {
    this.publications = [];
    this.xmlDoc = null;
    this.uri = null;
    
};

*/


var UstadJSOPF = null;

//empty constructor
UstadJSOPF = function() {
    this.spine = [];
    this.items = {};
    this.xmlDoc = null;
    this.baseUrl = null;
    this.title = "";
};

UstadJSOPF.prototype = {
    
    spine : [],
    items: {},
    xmlDoc : null,
    baseUrl: null,
    title: "",
    
    /**
     * 
     * @param {Object} String of OPF XML or XMLDocument already
     * 
     */
    loadFromOPF: function(opfSRC) {
        if(typeof opfSRC === "string") {
            var parser = new DOMParser();
            opfSRC  = parser.parseFromString(opfSRC, "text/xml");
        }
        
        this.xmlDoc = opfSRC;
        
        var manifest = this.xmlDoc.getElementsByTagName("manifest")[0];
        var itemNodes = manifest.getElementsByTagName("item");
        for(var i = 0; i < itemNodes.length; i++) {
            var opfItem = new UstadJSOPFItem(itemNodes[i].getAttribute("id"),
                itemNodes[i].getAttribute("media-type"),
                itemNodes[i].getAttribute("href"));
            if(itemNodes[i].hasAttribute("properties")) {
                opfItem.properties = 
                        itemNodes[i].getAttribute("properties");
            }
            this.items[opfItem.id] = opfItem;
        }
        
        //now find the spine
        /*
         * TODO: the spine should actually be references defined with whether or not
         * they are linear etc.  There may actually be multiple references to the same
         * item
         */
        var spine = this.xmlDoc.getElementsByTagName("spine")[0];
        var spineItems = spine.getElementsByTagName("itemref");
        for(var j = 0; j < spineItems.length; j++) {
            var itemID = spineItems[j].getAttribute("idref");
            if(spineItems[j].hasAttribute("linear")) {
                this.items[itemID].linear = 
                    (spineItems[j].getAttribute("linear") !== "no");
            }
            this.spine.push(this.items[itemID]);
        }
        
        //now load meta data: according to OPF spec there must be at least one title 
        //and one identifier
        var manifestEl = this.xmlDoc.getElementsByTagName("metadata")[0];
        var titleEl = manifestEl.getElementsByTagNameNS("*", "title")[0];
        var idEl = manifestEl.getElementsByTagNameNS("*", "identifier")[0];
        this.title = titleEl.textContent;
        this.identifier = idEl.textContent;
    },
    
    /**
     * Get the next linear entry index fromm the spine
     * @param start {number} the first value to look at
     * @param increment {number} 1 or -1 : the direction to look in case entries found are not linear
     * 
     * @return {number} The next linear index or -1 if nothing found
     */
    findNextLinearSpineIndex: function(start, increment) {
        var isLinear = false;
        var currentPos = start;
        while(!isLinear && (currentPos >= 0) && (currentPos< this.spine.length)) {
            if(this.spine[currentPos].linear) {
                return currentPos;
            }
            
            currentPos += increment;
        }
    
        return -1;
    },
    
    /**
     * For this OPF generate a catalog entry node that can be included
     * in an OPDS feed
     * 
     * @param {Object} acquisitionOpts options for acquisitionLink containing:
     *   href : e.g. /somewhere/file.epub
     *   mime : e.g. application/epub+zip
     *   rel : e.g. http://opds-spec.org/acquisition/open-access (defaults to
     *    http://opds-spec.org/acquisition )
     *   
     * @param {UstadJSOPDSFeed} parentFeed - document object to use for purpose of creating
     *   DOM nodes 
     * 
     * @returns {undefined}
     */
    getOPDSEntry: function(acquisitionOpts, parentFeed) {
        var doc = parentFeed.xmlDoc;
        
        var entryNode = doc.createElementNS("http://www.w3.org/2005/Atom", 
            "entry");
        
        var titleNode = doc.createElementNS("http://www.w3.org/2005/Atom", 
            "title");
        titleNode.textContent = this.title;
        entryNode.appendChild(titleNode);
        
        var idNode = doc.createElementNS("http://www.w3.org/2005/Atom", "id");
        idNode.textContent = this.identifier;
        entryNode.appendChild(idNode);
        
        
        //find metadata - mandatory in an opf file
        var metaDataNode = this.xmlDoc.getElementsByTagName("*", "metadata")[0];
        for(var i = 0; i < metaDataNode.childNodes.length; i++) {
            var clonedNode = doc.importNode(metaDataNode.childNodes[i], true);
            entryNode.appendChild(clonedNode);
        }
        
        
        //TODO: add acquisition links
        var linkNode = doc.createElementNS("http://www.w3.org/2005/Atom", 
            "link");
        linkNode.setAttribute("href", acquisitionOpts.href);
        linkNode.setAttribute("type", acquisitionOpts.mime);
        linkNode.setAttribute("rel", acquisitionOpts.rel ?
            acquisitionOpts.rel : UstadJSOPDSEntry.LINK_ACQUIRE);
        entryNode.appendChild(linkNode);
        
        var opdsEntry = new UstadJSOPDSEntry(entryNode, parentFeed);
        return opdsEntry;
    },
    
    /**
     * Lookup a given url to find it's position in the spine
     * 
     * @param {String} href
     * @returns {Number} index in spine
     */
    getSpinePositionByHref: function(href) {
        for(var i = 0; i < this.spine.length; i++) {
            if(this.spine[i].href === href) {
                return i;
            }
        }
        
        return -1;
    }
};

var UstadJSOPFItem = null;

UstadJSOPFItem = function(id, mediaType, href) {
    this.id = id;
    this.mediaType = mediaType;
    this.href = href;
    this.linear = true;
};

UstadJSOPFItem.prototype = {
    id : null,
    mediaType : null,
    href : null,
    scripted: null
};

var UstadJSTinCanXML = null;

UstadJSTinCanXML = function() {
    //original XML source document
    this.xmlDoc = null;
    
    //the launch activity (TinCan.Activity)
    this.launchActivity = null;
    this.launchActivityID = null;
    
    //the launchable activity
    this._launchActivityEl = null;
};


/**
 * Figure out which element to use by language for an activity (e.g. launch, resource)
 * 
 * matches the user language, if not look for default language, otherwise use
 * first occuring launch element
 * 
 * @param tagName {String} Tag name -e.g. launch or resource
 * @param activityEl {Object} DOM node representing the activity element
 * @param userLang {String} The language the user wants (e.g. UI Language)
 * @param defaultLang {String} the system default fallback language (e.g. 
 * 
 * @returns {String} 
 */
UstadJSTinCanXML.getElementByLang = function(tagName, activityEl, userLang, defaultLang) {
    var launchEls = activityEl.getElementsByTagName(tagName);
    
    if(!defaultLang) {
        defaultLang = "en";
    }
    
    var langsToMatch = [userLang, defaultLang];
    var matchedNodes = [null, null];
    var matchedStrs = [null, null];
    
    for(var i = 0; i < launchEls.length; i++) {
        var thisLang = launchEls[i].getAttribute("lang");
        if(thisLang) {
            var thisLangLower = thisLang.toLowerCase();
            for(var j = 0; j < langsToMatch.length; j++) {
                if(thisLangLower === langsToMatch[j].toLowerCase()) {
                    //full match of user string
                    matchedNodes[j] = launchEls[i];
                    matchedStrs[j] = thisLang;
                }else if(!matchedNodes[j] && thisLangLower.substring(0, 2) === langsToMatch[j].substring(0, 2)) {
                    //match first part of user string e.g. en-US instead of en-GB
                    matchedNodes[j] = launchEls[i];
                    matchedStrs[j] = thisLang.substring(0, 2);
                }
            }
        }
    }
    
    for(var h = 0; h < matchedNodes.length; h++) {
        if(matchedNodes[h]) { 
            return matchedNodes[h];
        }
    }
    
    //no match of user language or default - return the first launch element
    return launchEls[0];
};



UstadJSTinCanXML.prototype = {
    
    /**
     * 
     * @param {Object} tcXMLSrc String or xml document
     * @returns {undefined}
     */
    loadFromXML: function(tcXMLSrc) {
        if(typeof tcXMLSrc === "string") {
            var parser = new DOMParser();
            tcXMLSrc  = parser.parseFromString(tcXMLSrc, "text/xml");
        }
        
        this.xmlDoc = tcXMLSrc;
        
        var activityElements = this.xmlDoc.getElementsByTagName("activity");
        for(var i = 0; i < activityElements.length; i++) {
            var launchEls = activityElements[i].getElementsByTagName("launch");
            
            if(launchEls.length > 0) {
                this.launchActivityID = activityElements[i].getAttribute("id");
                this._launchActivityEl = activityElements[i];
                break;
            }
        }
    },
    
    /**
     * Sets the launch activity info by language
     * 
     * @param {String} userLang user set language
     * @param {String} defaultLang default fallback language (optional)
     */
    makeLaunchedActivityDefByLang: function(userLang, defaultLang) {
        var launchNameEl = UstadJSTinCanXML.getElementByLang("name", 
            this._launchActivityEl, userLang, defaultLang);
        var descEl = UstadJSTinCanXML.getElementByLang("description",
            this._launchActivityEl, userLang, defaultLang);
        var launchLang = launchNameEl.getAttribute("lang");
        
        var myDefinition = {
            type : "http://adlnet.gov/expapi/activities/lesson",
            name : { },
            description : { }
    	};
        
        myDefinition.name[launchLang] = launchNameEl.textContent;
        myDefinition.description[launchLang] = descEl.textContent;
        
        return myDefinition;
    }
};



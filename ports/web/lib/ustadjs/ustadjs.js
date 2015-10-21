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



/* 
 * A jQuery widget for displaying content in a feature phone like skin with
 * some required utilities.
 * 
 * MicroEmu format as per:
 
  http://www.petitpub.com/labs/j2me/me/tutorial/
  and
  http://pyx4me.com/snapshot/microemu/skin.html
 */



var $UstadJSMicroEmu = {
    IMG_STATES : ["normal", "over", "pressed"],
    
    KEYCODES_TO_BUTTONNAME: {
        "40" : "DOWN",
        "38" : "UP",
        "13" : "SELECT"
    },
    
    KEYCODE_DOWN : 40,
    
    KEYCODE_UP : 38,
    
    updateCanvas: function (func) {
        var fn2Use = window.requestAnimationFrame || 
            window.webkitRequestAnimationFrame || 
            window.msRequestAnimationFrame || 
            window.amozRequestAnimationFrame || 
            (function (func){setTimeout(func, 16.666);});

        fn2Use(func);
    }
};

/**
 * 
 * @class $UstadJSMicroEmuButton
 * @param {string} name button name e.g. SELECT SOFT1 as per device.xml
 */
var $UstadJSMicroEmuButton = function(name) {
    this.shape = null;
    this.name = name;
    
    this.state = 0;
};

/**
 * See if a given point (relative to the main widget) is contained within
 * the button
 * 
 * @param {Number} x click x coordinate
 * @param {Number} y click y coordinate
 * @returns {boolean}
 */
$UstadJSMicroEmuButton.prototype.containsPoint = function(x, y) {
    return this.shape.containsPoint(x,y);
};

/**
 * Add a button from the relevant XML node from device.xml
 * 
 * @param {Element} xmlEl The element from device.xml that represents the button
 * @returns {$UstadJSMicroEmuButton} a button from the XML element
*/
$UstadJSMicroEmuButton.makeButtonObjFromXML = function(xmlEl) {
    var buttonName = xmlEl.getAttribute("name");
    var buttonObj = new $UstadJSMicroEmuButton(buttonName);
    if(xmlEl.getElementsByTagName("polygon").length > 0) {
        var polygonEl = xmlEl.getElementsByTagName("polygon")[0];
        var pointEls = polygonEl.getElementsByTagName("point");
        var pts = [];
        for(var i = 0; i < pointEls.length; i++) {
            pts.push({
                x : parseInt(pointEls[i].getAttribute("x")),
                y : parseInt(pointEls[i].getAttribute("y"))
            });
        }
        buttonObj.shape = new $UstadJSMicroEmuButton.Polygon(pts);
    }else if(xmlEl.getElementsByTagName("rectangle").length > 0) {
        var rectEl = xmlEl.getElementsByTagName("rectangle")[0];
        buttonObj.shape = $UstadJSMicroEmuButton.Rectangle.makeFromXMLEl(rectEl);
    }

    return buttonObj;
};

/**
 * Represents a rectangle from device.xml - e.g. paintable area or button etc.
 * 
 * @class $UstadJSMicroEmuButton.Rectangle
 * 
 * @param {Object} coords Coordinates to use
 * @param {number} coords.x rectangle x coord
 * @param {number} coords.y rectangle y coord
 * @param {number} coords.width rectangle width
 * @param {number} coords.height rectnagle height
 * 
 * @returns {$UstadJSMicroEmuButton.Rectangle}
 */
$UstadJSMicroEmuButton.Rectangle = function(coords) {
    this.x = coords.x;
    this.y = coords.y;
    this.width = coords.width;
    this.height = coords.height;
};

/**
 * Make a rectangle object from an xml element with x, y, width, height
 * params as is found in device.xml files for MicroEmu
 * 
 * @param {Element} xmlEl XML element containing x, y, width and height elements
 * @returns {$UstadJSMicroEmuButton.Rectangle}
 */
$UstadJSMicroEmuButton.Rectangle.makeFromXMLEl = function(xmlEl) {
    var attrs = ["x", "y", "width", "height"];
    var coords = {};
    for(var j = 0; j < attrs.length; j++) {
        coords[attrs[j]] = 
            parseInt(xmlEl.getElementsByTagName(attrs[j])[0].textContent);
    }
    return new $UstadJSMicroEmuButton.Rectangle(coords);
};

/**
 * See if an x/y coordinate is within the rectangle
 * 
 * @param {Number} x coord x
 * @param {Number} y coord y
 * @returns {Boolean} true if point is within rectangle, false otherwise
 */
$UstadJSMicroEmuButton.Rectangle.prototype.containsPoint = function(x, y) {
    return (x >= this.x && x <= (this.x + this.width)) &&
        (y >= this.y && y <= (this.y + this.height));
};

/**
 * Represents a polygon object (e.g. used on buttons)
 * 
 * @param {Array} coords Array of objects with x and y (.x and .y) integer coordinates
 */
$UstadJSMicroEmuButton.Polygon = function(coords) {
    this.coords = coords;
};

/**
 * Borrowed from http://jsfromhell.com/math/is-point-in-poly
 * @param {type} x
 * @param {type} y
 * @returns {Boolean}
 */
$UstadJSMicroEmuButton.Polygon.prototype.containsPoint = function(x, y) {
    /* jshint ignore:start */
    var poly = this.coords;
    var pt = {"x" : x, "y" : y};
    for(var c = false, i = -1, l = poly.length, j = l - 1; ++i < l; j = i) {
        ((poly[i].y <= pt.y && pt.y < poly[j].y) || (poly[j].y <= pt.y && pt.y < poly[i].y)) && 
        (pt.x < (poly[j].x - poly[i].x) * (pt.y - poly[i].y) / (poly[j].y - poly[i].y) + poly[i].x) && 
        (c = !c);
    }
    return c;
    /* jshint ignore:end */
};


(function($){
    
    
    
    /**
     * Widget to create an UstadJSMicroEMU instance
     * 
     * Example:
     * 
     * $(".selector").microemu("loadmicroemuskin", 
     *  "/url/to/device.xml", {}, function() {
     *      //success fn when loaded - needs to wait until the skin file and images have loaded
     *      
     *      //get the div positioned on the paintable screen area
     *      var paintableScreenDiv = $(".selector").microemu("paintablearea");
     *      
     *      paintableScreenDiv.append("<iframe src...");
     *      
     *      //now set where selectable components are that the arrows control focus
     *      $(".selector").microemu("setselectablecontainer", 
     *          someIframe.contentDocument);
     *      
     *  }, function(err) {
     *  
     *  });
     * 
     *  The widget will trigger events of type "phonebuttonpress" which will 
     *  have a buttonName property corresponding with the button name defined
     *  in device.xml.
     *  
     *  If the event is the default action (e.g. there are no selectable elements
     *  in selectablecontainer) the event will have the property 
     *  isFormDefaultAction set as true, false otherwise.
     *  
     *
     * @class UstadJSOPDSBrowser
     * @memberOf jQuery.fn
     */
    $.widget("umjs.microemu", {
        
        /**
         * Whether or not load event has fired
         * @type {Boolean}
         */
        loadedEvtFired: false,
        
        /**
         * Callback function for success on loadmicroemuskin
         * @type {function}
         */
        _setupCallbackSuccessFn: null,
        
        /**
         * Callback function for failure on loadmicroemuskin
         * @type {function}
         */
        _setupCallbackFailFn : null,
        
        /**
         * Buttons from the device.xml skin
         * @type {Array<$UstadJSMicroEmuButton>}
         */
        _buttons: [],
        
        /**
         * Index of the button that mouse is currently over (hover)
         * 
         * @type {Number}
         */
        _mouseOverButtonIndex: -1,
        
        /**
         * Index of the button that mouse is currently pressing (mousedown)
         * 
         * @type {Number}
         */
        _mousePressedKeyIndex: -1,
        
        /**
         * The index of the element in selectableElementContainer that has 
         * focus
         * @type {number}
         */
        focusedElementIndex: -1,
        
        /**
         *  The selectable elements found in selectableElementContainer 
         *  @type Array{Element}
         */
        selectableElements: [],
        
        options : {
            /**
             *  The XML Document with the MicroEmu skin descriptor 
             *  @type {Document}
             */
            microEMUSkinXML: null,
            
            /** 
             * The absolute URL based from which assets are loaded 
             * @type {String}
             */ 
            assetBaseURL: "",
            
            /**
             * Object with .normal .over and .pressed of the image sources for
             * phone skin
             * @type {Object}
             */
            imgSrcs: {},
            
            /**
             * Object with .normal .over and .pressed of the image objects for
             * phone skin
             * @type {Object}
             */
            imgs: {},
            
            /**
             * Object with .normal .over and .pressed of the image load states for
             * phone skins as integers -1 = error, 0 = loading, 1 = loaded
             * @type {Object}
             */
            imgsLoadState: {},
            
            /** 
             * Main HTML5 Canvas to draw on
             * @type {Canvas}
             */
            canvas : null,
            
            /**
             * Width of the widget - set automatically from the skin on load
             * @type {Number}
             */
            width: 0,
            
            /**
             * Height of the widget - set automatically from the skin on load
             * @type {Number}
             */
            height: 0,
            
            /**
             * The scale to apply to buttons and images
             * 
             * @type {Number}
             */
            scale: 1.0,
            
            /**
             * An element that contains the screen area (containing the paintable
             * area and menubar area)
             * 
             * @type {Element}
             */
            screenAreaElement: null,
            
            /**
             * An element used to put the menubar in - positioned underneath
             * the paintableElement as per device.xml
             * 
             * @type {Element}
             */
            menubarAreaElement: null,
            
            /** 
             * The main paintable area on the screen where content is displayed
             * 
             * @type {Element}
             */
            paintableElement: null,
            
            /** 
             * Elements that can be selected using the up and down buttons on
             * the phone are looked for in here.
             * 
             * e.g. paintableElement is an automatically generated div, and then 
             * we might use an iframe or other neseted containers.  Within this 
             * container the widget will use
             * selectableElementSelector to focus input items etc. when the user
             * pushes the up/down buttons.
             * 
             * To implement a custom text for the middle select key use an
             * data-umjs-microemu-msk-label attribute on the item
             * 
             * @type {Element}
             */
            selectableContainer: null ,
            
            /**
             * The jQuery selector that is used to find user selectable elements
             * in selectableContainer
             */
            selectableElementSelector: "input, button",
            
            menubutton_labels: {
                "left" : "Options",
                /* Default for the form in case no selectable item is focused */
                "middle" : "OK",
                "right" : "Opt1",
                "select" : "Select"
            }
        },
        
        _create: function() {
            if(!this.element.hasClass("umjs-microemu")) {
                this.element.addClass("umjs-microemu");
            }
        },
        
        /**
         * Get button according to the name from device.xml e.g. "SELECT"
         * @param {String} buttonName name of button to find
         * @returns {$UstadJSMicroEmuButton}
         */
        getbuttonbyname: function(buttonName) {
            for(var i = 0; i < this._buttons.length; i++) {
                if(this._buttons[i].name === buttonName) {
                    return this._buttons[i];
                }
            }
            
            return null;
        },
        
        /**
         * Get button by index in the button array
         * 
         * @param {Number} index
         * @returns {$UstadJSMicroEmuButton}
         */
        getbuttonbyindex: function(index) {
            return this._buttons[index];
        },
        
        /**
         * Get the index of a button within the button array
         * @param {$UstadJSMicroEmuButton} button
         * @returns {Number} index in buttons array or -1 if it's not in
         */
        getindexofbutton: function(button) {
            for(var i = 0; i < this._buttons.length; i++) {
                if(this._buttons[i].name === button.name) {
                    return i;
                }
            }
        },
        
        /**
         * Setup the widget from a given skin.  Make sure the path is set so
         * imgSrcs can be resolved
         * 
         * @param {string|Document} skin the xml of 
         * @param {Object} options misc options (currently unused)
         */
        setupmicroemuskin: function(skin, options) {
            skin = UstadJS.ensureXML(skin);
            this.options.microEMUSkinXML = skin;
            
            this.options.imgSrcs = {};
            this.options.imgs = {};
            this.options.imgsLoadState = {};
            
            this._buttons = [];
            var buttonEls = skin.getElementsByTagName("button");
            for(var i = 0; i < buttonEls.length; i++) {
                var button = $UstadJSMicroEmuButton.makeButtonObjFromXML(
                    buttonEls[i]);
                this._buttons.push(button);
            }
            
            buttonEls = skin.getElementsByTagName("softbutton");
            for(var j = 0; j < buttonEls.length; j++) {
                var button1 = $UstadJSMicroEmuButton.makeButtonObjFromXML(
                    buttonEls[j]);
                this._buttons.push(button1);
            }
            
            for(var k = 0; k < $UstadJSMicroEmu.IMG_STATES.length; k++) {
                var currentImgName = $UstadJSMicroEmu.IMG_STATES[k];
                
                var imgSrc = skin.querySelector("img[name='" + 
                    currentImgName + "']").getAttribute('src');
                var imgSrcAbs = UstadJS.resolveURL(this.options.assetBaseURL, 
                    imgSrc);
                this.options.imgSrcs[currentImgName] = imgSrcAbs;
                
                var img = document.createElement("img");
                img.setAttribute("data-phoneimg-type", currentImgName);
                
                this.options.imgs[currentImgName] = img;
                this.options.imgsLoadState[currentImgName] = 0;
                
                img.onload = this.handlePhoneImageLoaded.bind(this);
                img.src = imgSrcAbs;
            }
        },
        
        /**
         * Multiply by the scale and round off the number provided
         * 
         * @param {Number} num
         * @returns {Number} num * scale rounded to nearest integer
         */
        _scaleNum: function(num) {
            return Math.round(num * this.options.scale);
        },
        
        /**
         * 
         * Run once all images have loaded - now we know the resolution and can
         * set everything in motion
         * 
         */
        _handleAllImagesLoaded: function() {
            if(this.loadedEvtFired) {
                return;
            }
            this.loadedEvtFired = true;
            
            this.options.width = this._scaleNum(this.options.imgs.normal.width);
            this.options.height = this._scaleNum(this.options.imgs.normal.height);
            
            //now create the canvas
            this.canvas = document.createElement("canvas");
            this.canvas.setAttribute("width", this.options.width);
            this.canvas.setAttribute("height", this.options.height);
            this.element.append(this.canvas);
            this.paintCanvas();
            
            this.canvas.addEventListener("mousemove", 
                this.handleMouseMove.bind(this), true);
            
            this.canvas.addEventListener("mousedown",
                this.handleMouseDown.bind(this), true);
                
            this.canvas.addEventListener("mouseup",
                this.handleMouseUp.bind(this), true);
            
            this.canvas.addEventListener("click",
                this.handleMouseClick.bind(this), true);
            
            var evt = jQuery.Event( "loaded", { microemu: this} );
            $(this.element).trigger("loaded", evt);
            
            //setup the paintable element
            this.paintableElement = document.createElement("div");
            this.paintableElement.style.position = "absolute";
            var displayEl = this.options.microEMUSkinXML.querySelector("display");
            var displayRect = $UstadJSMicroEmuButton.Rectangle.makeFromXMLEl(
                this.options.microEMUSkinXML.querySelector("display > rectangle"));
            
            var paintableRect = $UstadJSMicroEmuButton.Rectangle.makeFromXMLEl(
                this.options.microEMUSkinXML.querySelector("display > paintable"));
            
            this.paintableElement.style.marginLeft = 
                this._scaleNum(displayRect.x + paintableRect.x) + "px";
            this.paintableElement.style.marginTop =
                this._scaleNum(displayRect.y + paintableRect.y) + "px";
            this.paintableElement.style.width = this._scaleNum(
                paintableRect.width) + "px";
            this.paintableElement.style.height = this._scaleNum(
                    paintableRect.height) + "px";
            
            //this.paintableElement.style.border = "1px solid black";
            
            this.paintableElement.style.zIndex = 10000;
            this.paintableElement.innerHTML = "HI WORLD";
            
            //now make the menubar element
            this.menubarElement = document.createElement("div");
            this.menubarElement.style.position = "absolute";
            this.menubarElement.style.marginLeft = 
                this._scaleNum(displayRect.x + paintableRect.x) + "px";
            this.menubarElement.style.marginTop =
                this._scaleNum(displayRect.y + paintableRect.y + paintableRect.height) + "px";
            this.menubarElement.style.width = 
                this._scaleNum(paintableRect.width) + "px";
            this.menubarElement.style.height = 
                this._scaleNum(displayRect.height - 
                (paintableRect.height + paintableRect.y)) + "px";
            var menuTableEl = $("<table/>", {
                class: "umjs-microemu-menutable",
                width : "100%"
            });
            menuTableEl.css("height", this.menubarElement.style.height);
            $(menuTableEl).get(0).addEventListener("click", (function(evt) {
                this._checkfocus();
            }).bind(this), false);
            
            var menuTableTr = $("<tr/>",{
                class: "umjs-microemu-menutable-tr"
            }).appendTo(menuTableEl);
            
            
            var menuAreas = ["left", "middle", "right"];
            var keyMaps = ["SOFT1", "SELECT", "SOFT2"];
            
            for(var i = 0; i < menuAreas.length; i++) {
                var menuTd = $("<td/>", {
                    class: "umjs-microemu-menuarea umjs-microemu-menu-" +
                            menuAreas[i]
                }).appendTo(menuTableTr).text(
                    this.options.menubutton_labels[menuAreas[i]]);
                $(menuTd).attr("data-umjs-microemu-key", keyMaps[i]);
                $(menuTd).get(0).addEventListener("mousedown",
                    this.handleMenuBarMouseDown.bind(this), true);
                $(menuTd).get(0).addEventListener("mouseup",
                    this.handleMenuBarMouseUp.bind(this), true);
            }
            
            $(this.menubarElement).append(menuTableEl);
            
            $(this.element).prepend(this.paintableElement);
            $(this.paintableElement).after(this.menubarElement);
            
            UstadJS.runCallback(this._setupCallbackSuccessFn, this, []);
            
            this._setupCallbackSuccessFn = null;
            this._setupCallbackFailFn = null;
        },
        
        /**
         *  Handle mousedown on menubar area
         *  
         * @param {MouseEvent} evt
         */
        handleMenuBarMouseDown: function(evt) {
            evt.preventDefault();
            var evtObj = $.Event("phonebuttonpress", {
                "target" : this.element
            });
            var buttonName = evt.target.getAttribute("data-umjs-microemu-key");
            var buttonObj = this.getbuttonbyname(buttonName);
            var buttonIndex = this.getindexofbutton(buttonObj);
            
            this._setMousePressedButtonIndex(buttonIndex);
            $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
        },
        
        /**
         *  Handle mouseup on menubar area
         *  
         * @param {MouseEvent} evt
         */
        handleMenuBarMouseUp: function(evt) {
            evt.preventDefault();
            var buttonName = evt.target.getAttribute("data-umjs-microemu-key");
            this._setMousePressedButtonIndex(-1);

            
            var evtObj = $.Event("phonebuttonpress", {
                "target" : this.element
            });
            
            evtObj.buttonName = buttonName;
            $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
            this.handlePhoneButtonPress(evtObj);
        },
        
        /**
         * Look through an array of buttons to find the one which is at this
         * x/y position.
         * 
         * @param {number} x the x coord
         * @param {number} y the y coord
         * @param {Array<$UstadJSMicroEmuButton>} [buttonArr] of buttons to look in, this._buttons by default
         * @returns {number} the index of the button in the array, -1 if it's not there
         */
        getbuttonforposition: function(x, y, buttonArr) {
            buttonArr = (typeof buttonArr !== "undefined") ? buttonArr : this._buttons;
            for(var i = 0; i < this._buttons.length; i++) {
                if(buttonArr[i].containsPoint(x, y)) {
                    return i;
                }
            }
            
            return -1;
        },
        
        /**
         * Used to get the position of a mouseevent relative to it's target 
         * element
         * 
         * @param {MouseEvent} evt
         * @returns {Object} object wtih x and y coords relative to evt.target
         */
        getOffsetPosForEvt: function(evt) {
            var offsetVal =  {
                x: evt.pageX - $(evt.target).offset().left,
                y : evt.pageY - $(evt.target).offset().top
            };
            offsetVal.x = Math.round(offsetVal.x / this.options.scale);
            offsetVal.y = Math.round(offsetVal.y / this.options.scale);
            
            return offsetVal;
        },
        
        /**
         * Handle user mouse move and so we can show the appropriate over clip
         * 
         * @param {type} evt
         * @returns {undefined}
         */
        handleMouseMove: function(evt) {
            evt.preventDefault();
            var mousePos = this.getOffsetPosForEvt(evt);
            this._mouseOverButtonIndex = this.getbuttonforposition(mousePos.x,
                mousePos.y, this._buttons);
            for(var i = 0; i < this._buttons.length; i++) {
                if(i === this._mouseOverButtonIndex && this._buttons[i].state !== "pressed") {
                    this._buttons[i].state = "over";
                }else {
                    this._buttons[i].state = "normal";
                }
            }
            $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
        },
        
        /**
         * Handle when the mouse goes down (show pressed button)
         * 
         * @param {MouseEvent} evt
         */
        handleMouseDown: function(evt) {
            evt.preventDefault();
            var mousePos = this.getOffsetPosForEvt(evt);
            var newPress = this.getbuttonforposition(mousePos.x,
                mousePos.y, this._buttons);
            
            this._setMousePressedButtonIndex(newPress);
            
            
            $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
        },
        
        /**
         * Set the button that is being pushed by the mouse (can come from
         * clicking on canvas directly or clicking on the menubar
         * 
         * @param {Number} newIndex the new index that is being clicked on
         * 
         */
        _setMousePressedButtonIndex: function(newIndex) {
            if(this._mousePressedKeyIndex !== -1) {
                var pressedButton = this._buttons[this._mousePressedKeyIndex];
                if(pressedButton.state === "pressed") {
                    pressedButton.state = "normal";
                }
            }
            
            if(newIndex !== -1) {
                var newPressedButton = this._buttons[newIndex];
                newPressedButton.state = "pressed";
            }
            
            this._mousePressedKeyIndex = newIndex;
        },
        
        /**
         * Handle mouse up event - unpress button if one is pressed
         * 
         * @param {MouseEvent} evt
         */
        handleMouseUp: function(evt) {
            evt.preventDefault();
            if(this._mousePressedKeyIndex !== -1) {
                this._buttons[this._mousePressedKeyIndex].state = "normal";
                this._mousePressedKeyIndex = -1;
            }
            
            $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
        },
        
        /**
         * Handle mouse click on  the canvas - fire event for button push
         * 
         * @param {MouseEvent} evt
         */
        handleMouseClick: function(evt) {
            evt.preventDefault();
            var mousePos = this.getOffsetPosForEvt(evt);
            var pressedButtonIndex = this.getbuttonforposition(mousePos.x,
                mousePos.y);
            var pressedButton = this._buttons[pressedButtonIndex];
            if(pressedButton) {
                var evtObj = $.Event("phonebuttonpress", {
                    "target" : this.element
                });
                evtObj.buttonName = pressedButton.name;
                this.handlePhoneButtonPress(evtObj);
            }
            
            this._checkfocus();
        },
        
        /**
         * Handle when one of the skin images has loaded  - if all have
         * loaded trigger _handleAllImagesLoaded
         * 
         * @param {ProgressEvent} evt
         * @returns {undefined}
         */
        handlePhoneImageLoaded: function(evt) {
            var imgEl = evt.target;
            var imgTypeName = imgEl.getAttribute("data-phoneimg-type");
            this.options.imgsLoadState[imgTypeName] = 1;
            
            var loadCount = 0;
            for(var i = 0; i < $UstadJSMicroEmu.IMG_STATES.length; i++) {
                if(this.options.imgsLoadState[imgTypeName] === 1) {
                    loadCount++;
                }
            }
            
            if(loadCount === $UstadJSMicroEmu.IMG_STATES.length) {
                this._handleAllImagesLoaded();
            }
        },
        
        /**
         * Handle when the user has pressed a phone button
         * 
         * @param {type} evt
         * @returns {undefined}
         */
        handlePhoneButtonPress: function(evt) {
            var hasSelectableElements = this.selectedElementIndex !== -1;
            evt.isFormDefaultAction = (this.selectedElementIndex === -1);
            
            if(hasSelectableElements) {
                var selectedElement = this.selectableElements[
                    this.selectedElementIndex];
                if(evt.buttonName === "UP" || evt.buttonName === "DOWN") {
                    var increment = evt.buttonName === "UP" ? -1 : 1;
                    this.selectedElementIndex += increment;
                    if(this.selectedElementIndex < 0) {
                        this.selectedElementIndex = 
                        this.selectableElements.length-1;
                    }else if(this.selectedElementIndex >= this.selectableElements.length) {
                        this.selectedElementIndex = 0;
                    }

                    $(this.selectableElements[
                        this.selectedElementIndex]).focus();
                }else if(evt.buttonName === "SELECT") {
                    $(selectedElement).trigger("click");
                }
            }
            
            $(this.element).trigger(evt);
        },
        
        /**
         * Handle key down on selectable items - show pressed key for
         * relevant button
         * 
         * Attached to the selectable items themselves - prevents losing focus
         * 
         * @param {KeyEvent} evt
         */
        handleSelectableElementKeyDown: function(evt) {
            evt.preventDefault();
            var button = null;
            var whichStr = ""+evt.which;
            if($UstadJSMicroEmu.KEYCODES_TO_BUTTONNAME[whichStr]) {
                button = this.getbuttonbyname(
                    $UstadJSMicroEmu.KEYCODES_TO_BUTTONNAME[whichStr]);
            }
            
            if(button) {
                button.state = "pressed";
                $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
            }
        },
        
        /**
         * Handle when key is up on a selectableElement - move to the next button
         * and fire an event if this is a recognized button
         * 
         * @param {KeyEvent} evt
         */
        handleSelectableElementKeyUp: function(evt) {
            evt.preventDefault();
            
            var button = null;
            var whichStr = ""+evt.which;
            if($UstadJSMicroEmu.KEYCODES_TO_BUTTONNAME[whichStr]) {
                button = this.getbuttonbyname(
                    $UstadJSMicroEmu.KEYCODES_TO_BUTTONNAME[whichStr]);
            }
            
            if(button) {
                button.state = "normal";
                $UstadJSMicroEmu.updateCanvas(this.paintCanvas.bind(this));
                var evtObj = $.Event("phonebuttonpress", {
                    "target" : this.element
                });
                evtObj.buttonName = button.name;
                this.handlePhoneButtonPress(evtObj);
            }
            
        },
        
        /**
         * Sets the selectable container from which elements will be looked
         * for.
         * 
         * @param {Element} selectableContainer
         */
        setselectablecontainer: function(selectableContainer) {
            this.selectableContainer = selectableContainer;
            this.updateselectable();
        },
        
        /**
         * When the contents of the selectable container change - run this.
         * 
         * Looks for selectable elements within the selectable container
         * and updates the menubar
         * 
         */
        updateselectable: function() {
            this.selectableElements = $(this.selectableContainer).find(
                this.options.selectableElementSelector);
            if(this.selectableElements.length > 0) {
                this.selectedElementIndex = 0;
                for(var i = 0; i < this.selectableElements.length; i++) {
                    this.selectableElements[i].onkeydown = 
                        this.handleSelectableElementKeyDown.bind(this);
                    
                    this.selectableElements[i].onkeyup = 
                        this.handleSelectableElementKeyUp.bind(this);
                }
                
            }else {
                this.selectedElementIndex = -1;
            }
            
            this.updatemenubar();
        },
        
        /**
         * Make sure that the last selected item still has focus - useful if
         * the user clicks outside etc.
         * 
         */
        _checkfocus: function() {
            if(this.selectableElements.length > 0) {
                var currentEl = this.selectableElements[this.selectedElementIndex];
                if(currentEl.ownerDocument.activeElement !== currentEl) {
                    $(currentEl).focus();
                }
            }
        },
        
        /**
         * Get the selected index from selectable elements
         * 
         * @returns {Number} index of currently selected component, or -1 if there are none currently
         */
        getselectedindex: function() {
            return this.selectedElementIndex;
        },
        
        /**
         * Gets the selectable elements array found on the current selectable container
         * 
         * @returns {Array<Element>}
         */
        getselectableelements: function() {
            return this.selectableElements;
        },
        
        /**
         * Gets the currently selected element within the selectable container
         * if there is one
         * 
         * @return {Number} index of hte selected item or -1 if there are none to select from
         */
        getselectedelement: function() {
            return this.selectedElementIndex !== -1 ?
                this.selectableElements[this.selectedElementIndex ] : null;
        },
        
        /**
         * Update the menu bar - in particular the middle select key for the
         * currently selected item
         * 
         */
        updatemenubar: function() {
            var middleMenuText = this.options.menubutton_labels.middle;
            
            if(this.selectedElementIndex !== -1) {
                var selectedEl = this.selectableElements[
                    this.selectedElementIndex];
                $(selectedEl).focus();
                
                middleMenuText = selectedEl.hasAttribute(
                    "data-umjs-microemu-msk-label") ? 
                    selectedEl.getAttribute("data-umjs-microemu-msk-label") :
                    this.options.menubutton_labels.select;
            }
            this.element.find(".umjs-microemu-menu-middle").text(middleMenuText);
        },
        
        /**
         * Load a MicroEMU device.xml skin from the given URL
         * 
         * @param {String} url the URL to load from 
         * @param {Object} options misc options space - currently not used
         * @param {function} successFn function to run once successfully setup
         * @param {function} failFn function to run if fails - e.g. image not found
         */
        loadmicroemuskin: function(url, options, successFn, failFn) {
            this.options.assetBaseURL = UstadJS.resolveURL(document.location.href,
                url);
            this._setupCallbackSuccessFn = successFn;
            this._setupCallbackFailFn = failFn;
            
            $.ajax(url, {
                dataType: "text"
            }).done((function(data, textStatus, jqXHR) {
                this.setupmicroemuskin(data, options);
            }).bind(this)).fail(failFn);
        },
        
        /**
         * Clip the canvas context according to a given button
         * 
         * @param {2DContext} ctx
         * @param {$UstadJSMicroEMUButton} button
         */
        _clipContextForButton: function(ctx, button) {
            var coords = [];
            if(button.shape instanceof $UstadJSMicroEmuButton.Polygon) {
                coords = button.shape.coords;
            }else if(button.shape instanceof $UstadJSMicroEmuButton.Rectangle) {
                var rect = button.shape;                    

                coords = [
                    {x : rect.x, y: rect.y },//top left
                    {x : rect.x + rect.width, y: rect.y},//top right
                    {x : rect.x + rect.width, y: rect.y + rect.height},//bottom right
                    {x : rect.x, y : rect.y + rect.height}//bottom left
                ];
            }

            var scaledCoords = [];
            for(var i = 0; i < coords.length; i++) {
                scaledCoords.push({
                    x : this._scaleNum(coords[i].x),
                    y : this._scaleNum(coords[i].y)
                });
            }
            
            ctx.beginPath();
            ctx.moveTo(scaledCoords[0].x, 
                scaledCoords[0].y);
            for(var j = 1; j < scaledCoords.length; j++) {
                ctx.lineTo(scaledCoords[j].x, scaledCoords[j].y);
            }
            ctx.closePath();
            ctx.clip();
        },
        
        /**
         * Paint the skin of the phone on the canvas and show pressed and over
         * keys
         * 
         */
        paintCanvas: function() {
            var ctx = this.canvas.getContext("2d");
            ctx.save();
            ctx.fillStyle = "#ffffff";
            ctx.fill();
            
            ctx.drawImage(this.options.imgs.normal, 0, 0, this.options.width,
                this.options.height);
            
            //paint the over image
            if(this._mouseOverButtonIndex !== -1) {
                var overButton = this._buttons[this._mouseOverButtonIndex];
                this._clipContextForButton(ctx, overButton);
                ctx.drawImage(this.options.imgs.over, 0, 0, this.options.width,
                    this.options.height);
            }
            
            //paint pressed keys
            for(var i = 0; i < this._buttons.length; i++) {
                if(this._buttons[i].state === "pressed") {
                    this._clipContextForButton(ctx, this._buttons[i]);
                    ctx.drawImage(this.options.imgs.pressed, 0, 0, 
                        this.options.width,this.options.height);
                }
            }
            
            ctx.restore();
        },
        
        /**
         * Get the canvas being used to show the phone skin
         * 
         * @returns {Canvas}
         */
        getcanvas: function() {
            return this.canvas;
        },
        
        /**
         * Get the paintable area div in which items can be placed
         * 
         * @returns {Element}
         */
        paintablearea: function() {
            return this.paintableElement;
        }
        
        
        
    });
}(jQuery));



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

var $UstadJSOPDSBrowser = {};

$UstadJSOPDSBrowser.STATUS_UNKNOWN = "unknown";
$UstadJSOPDSBrowser.NOT_ACQUIRED = "notacquired";
$UstadJSOPDSBrowser.ACQUISITION_IN_PROGRESS = "inprogress";
$UstadJSOPDSBrowser.ACQUIRED = "acquired";

$UstadJSOPDSBrowser.STATUSCLASS_PREFIX = "umjs_opdsbrowser_status_";

(function($){
    /**
     *
     * @class UstadJSOPDSBrowser
     * @memberOf jQuery.fn
     */
    $.widget("umjs.opdsbrowser", {
        options: {
            "src" : "",
            //custom feed loader (e.g. to check cached entries etc)
            feedloader: null,
            
            /**
             * @type {string}
             * icon to use when none specified by feed for an acquisition feed
             */
            defaulticon_acquisitionfeed: "default-acquire-icon.png",
            
            /**
             * @type {string}
             * icon to use when none is specified by feed for a navigation feed
             */
            defaulticon_navigationfeed: "default-navigation.png",
            
            /**
             * @type {string}
             * icon to show for entries in an acquisition feed with conatainers to download
             */
            defaulticon_containerelement: "default-containerel.png",
            
            /**
             * @type {UstadJSOPDSFeed}
             */
            _opdsFeedObj : null,
            
            /**
             * Callback to run when the user selects another feed
             * 
             * @type {function}
             */
            feedselected: null,
            
            /**
             * Callback to run when the user selects an acquisition container
             * (e.g. epub file)
             * 
             * @type {function}
             */
            containerselected : null,
            
            /**
             * Callback to run when the user requests the context menu for an item
             * (e.g. right clicks or taphold)
             */
            containercontextrequested: null,
            
            
            /**
             * A function that should return whether or not the given device id
             * has been acquired
             * 
             * @returns {boolean}
             */
            acquisitionstatushandler: function(id) {
                return $UstadJSOPDSBrowser.NOT_ACQUIRED;
            },
            
            button_text_navigation: {
                "unknown" : "Checking...",
                "notacquired" : "Open",
                "inprogress" : "Open",
                "acquired" : "Open"
            },
            
            button_text_acquisition: {
                "unknown" : "Checking...",
                "notacquired" : "Download",
                "inprogress" : "Downloading...",
                "acquired" : "Open"
            },
            
            /** If true and jQueryMobile is present, will call enhanceWithin */
            autoJQM : true
            
        },
        
        _create: function () {
            if(!this.element.hasClass("umjs_opdsbrowser")) {
                this.element.addClass("umjs_opdsbrowser");
            }
        },
        
        /**
         * Append appropriate CSS classed title to the main element here
         */
        _appendTitle: function(titleStr) {
            var titleEl = $("<div/>", {
                class : "umjs_opdsbrowser_title"
            });
            titleEl.text(titleStr);
            this.element.append(titleEl);
        },
        
        /**
         * Sets up a navigation feed view where acquisition feeds are shown
         * as tiles, other navigation feeds are shown as screen width category
         * buttons at the bottom.
         * 
         * Use with OPDS navigation feeds: e.g. link type
         * application/atom+xml;profile=opds-catalog;kind=navigation
         * see: http://opds-spec.org/specs/opds-catalog-1-1-20110627/#Navigation_Feeds
         * 
         * @param {UstadJSOPDSFeed} opdsSrc the source feed
         */
        setupnavigationfeedview: function(opdsSrc) {
            this.options._opdsFeedObj = opdsSrc;
            this._updateFeedAbsoluteBaseURL();
            
            this.element.empty();
            this.element.addClass("umjs_opdsbrowser_navfeed");
            
            this._appendTitle(opdsSrc.title);
            
            var feedItems = opdsSrc.getEntriesByLinkParams(null, 
                "application/atom+xml", {mimeTypeByPrefix: true});
            
            var feedElContainer= $("<ul/>", {
                "class" : "umjs_opdsbrowser_item_feed",
                "data-role" : "listview",
                "data-inset" : "true"
            });
            this.element.append(feedElContainer);
            
            var lastFeedItem = null;
            
            for(var g = 0; g < feedItems.length; g++) {
                var elEntry = this._makeFeedElement(feedItems[g], {
                    feedType : "navigation",
                    clickHandler: this._handleFeedClick.bind(this)
                });
                    
                feedElContainer.append(elEntry);
                lastFeedItem = feedElContainer;
            }
            
            //put the clearfix on so it will compute height
            lastFeedItem.addClass("umjs_clearfix");
            
            
            if(this.options.autoJQM && this.element.enhanceWithin) {
                this.element.enhanceWithin();
            }
            
        },
        
        setupacquisitionfeedview: function(opdsSrc) {
            this.options._opdsFeedObj = opdsSrc;
            this._updateFeedAbsoluteBaseURL();
            this.element.empty();
            this._appendTitle(opdsSrc.title);
            this.element.addClass("umjs_opdsbrowser_acqfeed");
            
            var elContainer = $("<ul/>", {
                "class" : "umjs_opdsbrowser_item_feed",
                "data-role" : "listview",
                "data-inset" : "true"
            }).appendTo(this.element);
            
            for(var f = 0; f < opdsSrc.entries.length; f++) {
                var containerEl = this._makeFeedElement(opdsSrc.entries[f], {
                    feedType: "acquisition",
                    clickHandler: this._handleContainerElClick.bind(this),
                    contextHandler: this._handleContainerElContext.bind(this),
                    showSummary: true
                });
                elContainer.append(containerEl);
            }
            
            if(this.options.autoJQM && this.element.enhanceWithin) {
                this.element.enhanceWithin();
            }
        },
        
        /**
         * Setup this OPDS feed browser from the given feed object.  If 
         * an acquisition feed uses setupacquisitionfeedview otherwise
         * it's a navigation feed and use setupnavigationfeedview
         * 
         * @param {UstadJSOPDSFeed} opdsSrc Source OPDS element
         * @returns {undefined}
         */
        setupfromfeed: function(opdsSrc) {
            if(opdsSrc.isAcquisitionFeed()) {
                this.setupacquisitionfeedview(opdsSrc);
            }else {
                this.setupnavigationfeedview(opdsSrc);
            }
        },
        
        _updateFeedAbsoluteBaseURL: function() {
            this._feedAbsoluteBaseURL = UstadJS.makeAbsoluteURL(
                this.options._opdsFeedObj.href);
        },
        
        /**
         * 
         * @param {type} entryId
         * @param {type} elStatus
         * @param {type} options
         * @returns {undefined}
         */
        updateentrystatus: function(entryId, elStatus, options) {
            var entryEl = $("li.umjs_opdsbrowser_feedelement[data-entry-id='" +
                entryId + "']");
            
            var statusList = [
                $UstadJSOPDSBrowser.STATUS_UNKNOWN = "unknown",
                $UstadJSOPDSBrowser.NOT_ACQUIRED = "notacquired",
                $UstadJSOPDSBrowser.ACQUISITION_IN_PROGRESS = "inprogress",
                $UstadJSOPDSBrowser.ACQUIRED = "acquired"
            ];
            for(var i = 0; i < statusList.length; i++) {
                if(elStatus === statusList[i]){
                    entryEl.addClass($UstadJSOPDSBrowser.STATUSCLASS_PREFIX +
                        statusList[i]);
                }else{
                    entryEl.removeClass($UstadJSOPDSBrowser.STATUSCLASS_PREFIX +
                        statusList[i]);
                }
            }
        },
        
        /**
         * Set if the progress area for a given entry should be visible or not
         * 
         * @param {type} entryId
         * @param {type} visible
         * @returns {undefined}
         */
        progressentryvisible: function(entryId, visible) {
            $("li.umjs_opdsbrowser_feedelement[data-entry-id='" +
                entryId + "'] .umjs_opdsbrowser_progressarea").css("visibility",
                visible ? "visible" : "hidden");
        },
        
        updateentryprogress: function(entryId, progressEvt) {
            var progressEl = $("li.umjs_opdsbrowser_feedelement[data-entry-id='" +
                entryId + "'] .umjs_opdsbrowser_progressarea progress");
            progressEl.attr("value", progressEvt.loaded);
            progressEl.attr("max", progressEvt.total);
        },
        
        _handleContainerElContext: function(evt, data) {
            var clickedLink = $(evt.delegateTarget);
            var clickedEntry = clickedLink.parent("li");
            var entryId = clickedEntry.attr("data-entry-id");
            var entry = this.options._opdsFeedObj.getEntryById(entryId);
            
            this._trigger("containercontextrequested", null, {
                entryId : entryId,
                entry : entry,
                srcEvent: evt
            });
        },
        
        _handleContainerElClick: function(evt, data) {
            var clickedLink = $(evt.delegateTarget);
            var clickedEntry = clickedLink.parent("li");
            var entryId = clickedEntry.attr("data-entry-id");
            var entry = this.options._opdsFeedObj.getEntryById(entryId);
            
            this._trigger("containerselected", null, {
                entryId : entryId,
                entry : entry,
                srcEvent: evt
            });
        },
        
        /**
         * Fire the feedselected event when the user clicks on a feed 
         * displayed
         * 
         * @param {Event} evt
         * @param {Object} data
         */
        _handleFeedClick: function(evt, data) {
            var clickedFeedEntryLink = $(evt.delegateTarget);
            var clickedFeedEntry = clickedFeedEntryLink.parent("li");
            var clickedFeedId = $(clickedFeedEntry).attr("data-entry-id");
            var clickedFeedType = $(clickedFeedEntry).attr("data-entry-type");
            this._trigger("feedselected", null, {
                feedId : clickedFeedId,
                feedType : clickedFeedType,
                entry : this.options._opdsFeedObj.getEntryById(clickedFeedId)
            });
        },
        
        /**
         * Generate the credit text for an opds entry - first look for publisher
         * if none, then use author
         * 
         * @param {type} entry
         * @returns {undefined}
         */
        _makeCreditForEntry: function(entry) {
            return entry.getPublisher();
        },
        
        /**
         * Make a div element that will represent an feed object to be clicked on
         * 
         * @param {UstadJSOPDSEntry} entry the entry to make an element for
         * @param {Object} options
         * @param {function} options.clickHandler event handling method
         * @param {function} options.contextHandler event when context menu might open - e.g. taphold or right click
         * @param {String} [options.feedType=navigation] feed type
         * @param {boolean} [options.showSummary=false] If true add the content
         * of the OPDS entry
         * 
         * @returns {$|jQuery}
         */
        _makeFeedElement: function(entry, options) {
            var feedType = options.feedType || "navigation";
            var elEntry = $("<li/>", {
                class : "umjs_opdsbrowser_" + feedType + "feed_element",
                "data-entry-id" : entry.id,
                "data-entry-type" : feedType
            });
            
            elEntry.addClass("umjs_opdsbrowser_feedelement");
            
            var elLink = $("<a/>", {
                "href" : "#"
            });
            elEntry.append(elLink);
            
            if(options.clickHandler) {
                elLink.on("click", options.clickHandler);
            }
            
            if(options.contextHandler) {
                elLink.on("taphold", options.contextHandler);
            }
             
            var entryThumbnail = entry.getThumbnail();
            var imgSrc = entryThumbnail ? 
                UstadJS.resolveURL(this._feedAbsoluteBaseURL, entryThumbnail) : 
                this.options["defaulticon_" + feedType + "feed"];
            
            elLink.append($("<img/>", {
                "src": imgSrc,
                "class": "umjs_opdsbrowser_" + feedType + "feed_img ui-li-thumb"
            }));
            
            var elTitleEntry = $("<h2/>", {
                "class" : "umjs_opdsbrowser_" + feedType + "title"
            });
            elTitleEntry.text(entry.title);
            
            elLink.append(elTitleEntry);
            
            var elStatus = this.options.acquisitionstatushandler(entry.id, 
                feedType);
            //Disable this for now
            
            
            elEntry.append("<div class='umjs_opdsbrowser_entrystatusarea'></div>");
            elEntry.addClass($UstadJSOPDSBrowser.STATUSCLASS_PREFIX + elStatus);
            
            /*
            elEntry.append(this._makeFeedElementStatusArea(entry.id, feedType,
                elStatus));
            */
            
            var pContent = $("<p/>");
            if(options.showSummary) {
                var entrySummary = entry.getSummary() || "";
                pContent.append(entrySummary);
            }
            
            var providerImgLinks = entry.getLinks(
                "http://www.ustadmobile.com/providerimg", null);
        
            if(providerImgLinks.length > 0) {
                var providerImgSrc= UstadJS.resolveURL(
                    this._feedAbsoluteBaseURL, providerImgLinks[0].href);
                $("<img/>", {
                    "class" : "provider-logo",
                    "src" : providerImgSrc
                }).appendTo(pContent);
            }
            
            
            if(entry.getPublisher()) {
                pContent.append(this._makeCreditForEntry(entry));
            }
            elLink.append(pContent);
            
            var statusArea = $("<div/>", {
                "class" : "umjs_opdsbrowser_progressarea"
            });
            statusArea.append("<progress max='100' value='0'/>");
            elLink.append(statusArea);
            
            return elEntry;
        }
        
    });
}(jQuery));    

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

var UstadJSOPUBFrame = function(element) {
    this.element = element;
    this.iframeElement = document.createElement("iframe");
    this.element.append(this.iframeElement);
    
    this.iframeElement.style.width = "100%";
    this.iframeElement.style.height = "100%";
    this.iframeElement.style.margin = "0px";
    this.iframeElement.style.border = "none";
};

UstadJSOPUBFrame.defaultOptions = {
    "editable" : false,
    "spine_pos" : 0,
    "baseurl" : null,
    //the UstadJSOPF object being represented
    "opf" : null,
    "height" : "100%",
    "num_pages" : 0,
    //the query parameters to add (e.g. tincan params) 
    "page_query_params": null,
    "pageloaded" : null
};

UstadJSOPUBFrame.prototype.handleLoadIframe = function(evt) {
    
};


(function($){
    /**
     * attemptidevice - an awesome jQuery plugin. 
     *
     * @class umjsEpubframe
     * @memberOf jQuery.fn
     */
    $.widget("umjs.opubframe", {
        options : {
            "editable" : false,
            "spine_pos" : 0,
            "baseurl" : null,
            //the UstadJSOPF object being represented
            "opf" : null,
            "height" : "100%",
            "num_pages" : 0,
            //the query parameters to add (e.g. tincan params) 
            "page_query_params": null,
            "pageloaded" : null
        },
        
        
        /**
         * Main widget creation
         */
        _create : function() {
            /*
            this.iframeElement = document.createElement("iframe");
            this.element.append(this.iframeElement);
            $(this.iframeElement).css("width", "100%").css("height", "100%");
            $(this.iframeElement).css("margin", "0px");
            $(this.iframeElement).css("border", "none");
            */
            this.opubFrame = new UstadJSOPUBFrame(this.element);
            this.iframeElement = this.opubFrame.iframeElement;
            this.iframeElement.addEventListener("load",
                $.proxy(this.iframeLoadEvt, this), true);
            this.runOnceOnFrameLoad = [];
            $(this.element).addClass("umjs-opubframe");
        },
        
        _setOption: function(key, value) {
            this._super(key, value);
            if(key === "height") {
                $(this.element).css("height", value);
                $(this.iframeElement).css("height", 
                    $(this.element).outerHeight(false)-4);
            }
        },
        
        /**
         * Add the appropriate query parameters to the given url
         * 
         * @param {String} url URL to add parameters to
         * @returns {String} the url with query parameters (if any)
         */
        appendParamsToURL: function(url) {
            if(this.options.page_query_params) {
                return url + "?" + this.options.page_query_params;
            }else {
                return url;
            }
        },
        
        iframeLoadEvt: function(evt) {
            //figure out where we are relative to package.opf
            var iframeSrc = evt.target.contentWindow.location.href;
            var relativeURL = iframeSrc.substring(iframeSrc.indexOf(
                    this.options.baseurl) + this.options.baseurl.length);
            relativeURL = UstadJS.removeQueryFromURL(relativeURL);
            this.options.spine_pos = this.options.opf.getSpinePositionByHref(
                    relativeURL);
            $(this.element).trigger("pageloaded", evt, {"relativeURL" :
                        relativeURL});
            this._trigger("pageloaded", null, {"url" : relativeURL});
        },
        
        
        /**
         * Load publication by path specified to the OPF file
         * 
         * @param {String} opfURL URL of OPF file
         * @param {function} callback
         */
        loadfromopf: function(opfURL, callback) {
            
            //convert the URL to being absolute for the iframe
            var opfBaseURL = "";
            opfBaseURL += opfURL.substring(0, opfURL.lastIndexOf("/")+1);
            opfBaseURL = UstadJS.makeAbsoluteURL(opfBaseURL);
            
            this.options.baseurl = opfBaseURL;
            $.ajax(opfURL, {
                dataType : "text"
            }).done($.proxy(function(data) {
                this.options.opf = new UstadJSOPF();
                this.options.opf.loadFromOPF(data);
                var firstLinearItem = this.options.opf.findNextLinearSpineIndex(
                    0, 1);
                var firstURL = opfBaseURL + this.options.opf.spine[firstLinearItem].href;
                firstURL = this.appendParamsToURL(firstURL);
                
                this.options.num_pages = this.options.opf.spine.length;
                
                this.iframeElement.setAttribute("src",firstURL);
                $(this.iframeElement).one("load", null, $.proxy(function() {
                    UstadJS.runCallback(callback, this, ["success", 
                    this.options.opf]);
                }, this));
            }, this));
        },
        
        /**
         * Load publication from container manifest
         * 
         * First open the META-INF/container.xml to find root files
         * 
         * Then load the OPF of the root file at position given by 
         * containerRootIndex
         * 
         * Then display the cover page
         * 
         * @param {type} baseURL base URL to directory with extracted container
         * @param {type} containerRootIndex root package file to load (e.g. 0 for first publication)
         * @param {function} callback function to call when done - args are status, opf
         */
        loadfrommanifest: function(baseURL, containerRootIndex, callback) {
            if(baseURL.charAt(baseURL.length-1) !== '/') {
                baseURL += '/';
            }
            
            var containerURL = baseURL + "META-INF/container.xml";
            $.ajax(containerURL, {
                dataType : "text"
            }).done($.proxy(function(data) {
                var rootFilesArr = UstadJS.getContainerRootfilesFromXML(data);
                var opfURL = baseURL + rootFilesArr[containerRootIndex]['full-path'];
                console.log("opfURL is : " + opfURL);
                this.loadfromopf(opfURL, callback);
            }, this));
        },
        
        /**
         * Navigate along the spine (e.g. back/next) - looks only for linear
         * elements in the spine as per the epub spec
         * 
         * @param {type} increment
         * @returns {undefined}
         */
        go: function(increment, callback) {
            var nextIndex = this.options.opf.findNextLinearSpineIndex(
                this.options.spine_pos+increment, increment);
            if(nextIndex === -1) {
                UstadJS.runCallback(callback, this, ["fail: no more pages"]);
                return;
            }
            
            var nextURL = this.options.baseurl + 
                    this.options.opf.spine[nextIndex].href;
            nextURL = this.appendParamsToURL(nextURL);
            this.iframeElement.setAttribute("src", nextURL);
            $(this.iframeElement).one("load", null, $.proxy(function() {
                        UstadJS.runCallback(callback, this, ["success"]);
                    }, this));
        },
        
        currenttitle: function() {
            var pgTitle = null;
            var titleEls = this.iframeElement.contentDocument.getElementsByTagName("title");
            if(titleEls.length > 0) {
                pgTitle = titleEls[0].textContent;
            }
            
            return pgTitle;
        }
    });
}(jQuery));

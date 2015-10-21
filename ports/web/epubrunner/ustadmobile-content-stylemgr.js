/* 
 * Allow switching of JQueryMobile themes according to URL Parameters
 */
(function() {
    var replaceCssFn = function(el, newHref) {
        var newLink = document.createElement("link");
        newLink.setAttribute("rel", "stylesheet");
        newLink.setAttribute("type", "text/css");
        newLink.setAttribute("href", newHref);
        newLink.setAttribute("id", el.getAttribute("id"));
        document.getElementsByTagName("head").item(0).replaceChild(newLink, el);
    };
    
    var queryVars = UstadMobileUtils.getQueryVariables();
    if(queryVars.jqmThemeLink) {
        replaceCssFn(document.getElementById("jqmThemeLink"), 
            queryVars.jqmThemeLink);
    }
    if(queryVars.jqmStructLink) {
        replaceCssFn(document.getElementById("jqmStructLink"), 
            queryVars.jqmStructLink);
    }
    
})();



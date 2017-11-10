
var _ustadMobileEpubPaginate = (function() {
    var mod = {};

    //do not apply to framed pages
    if(window.self !== window.top)
        return;

    var paginateHeadEl = document.getElementsByTagName("head")[0];
    var paginateCssEl = document.createElement("link");
    paginateCssEl.setAttribute("rel", "stylesheet");
    var jsSource = document.body.getElementsByTagName("script")[0].getAttribute("src");
    paginateCssEl.setAttribute("href", jsSource.substring(0, jsSource.lastIndexOf('/')) + "/epub-paginate.css");
    paginateHeadEl.appendChild(paginateCssEl);

    var bodyStyle = window.getComputedStyle(document.body);
    var androidVersionMatch = navigator.userAgent.toLowerCase().match(/android\s([0-9\.]*)/);
    var androidVersion = androidVersionMatch ? parseFloat(androidVersionMatch[1]) : false;
    console.log("epub-paginate: Android version = " + androidVersion);
    var isOldAndroid = androidVersion ? androidVersion < 4.4 : false;
    var verticalMargin = parseInt(bodyStyle.marginTop) + parseInt(bodyStyle.marginBottom);
    var verticalPadding = parseInt(bodyStyle.paddingTop) + parseInt(bodyStyle.paddingBottom);
    var bodyHeight = (isOldAndroid ? window.outerHeight : window.innerHeight)
        - verticalMargin - verticalPadding;

    console.log("Body width = " + window.innerWidth);
    console.log("Body margin left = " + parseInt(bodyStyle.marginLeft) +  "px, right = " + parseInt(bodyStyle.marginRight) + "px");
    console.log("Body padding left = " + parseInt(bodyStyle.paddingLeft) + "px, right = " + parseInt(bodyStyle.paddingRight) + "px");

    var horizontalPadding = parseInt(bodyStyle.paddingLeft) + parseInt(bodyStyle.paddingRight);
    var horizontalMargin = parseInt(bodyStyle.marginLeft) + parseInt(bodyStyle.marginRight);
    var bodyWidth = window.innerWidth - horizontalMargin - horizontalPadding;

    /*
     If Android less than 4.4, we must use window.outerHeight (because it will cut out the soft keyboard space). Otherwise window.innerHeight is what we want to use.
     */

    document.body.style.height = bodyHeight + "px";
    document.body.style.columnWidth = bodyWidth + "px";
    document.body.style.WebkitColumnWidth = bodyWidth + "px";

    document.body.style.columnGap = (horizontalPadding + horizontalMargin) + "px";
    document.body.style.WebkitColumnGap = (horizontalPadding + horizontalMargin) + "px";
    console.log("Set column width to " + bodyWidth + "px, body height to " + bodyHeight + "px");

    //now set a max width for all child elements
    var styleEl = document.createElement('style');
    styleEl.appendChild(document.createTextNode(""));
    document.head.appendChild(styleEl);
    styleEl.sheet.insertRule("body > * { max-width: " + bodyWidth + "px }");


    window.addEventListener("load", function() {
        console.log("epub-paginate: body width is now: " + document.body.innerWidth);
        var lastElement = document.body.lastElementChild;
        var boundingRect = lastElement.getBoundingClientRect();
        console.log("last element coordinates: " + boundingRect.left + ", " + boundingRect.top);
    }, false);


    return mod;
}());



var paginateHeadEl = document.getElementsByTagName("head")[0];
var paginateCssEl = document.createElement("link");
paginateCssEl.setAttribute("rel", "stylesheet");
var jsSource = document.body.getElementsByTagName("script")[0].getAttribute("src");
paginateCssEl.setAttribute("href", jsSource.substring(0, jsSource.lastIndexOf('/')) + "/epub-paginate.css");
paginateHeadEl.appendChild(paginateCssEl);

var bodyStyle = window.getComputedStyle(document.body);
var androidVersion = navigator.userAgent.toLowerCase().match(/android\s([0-9\.]*)/);
console.log("epub-paginate: Android version = '" + androidVersion + "' parsed = " + parseFloat(androidVersion));
var isOldAndroid = androidVersion ? parseFloat(androidVersion) < 4.4 : false;
var bodyHeight = (isOldAndroid ? window.outerHeight : window.innerHeight) - (parseInt(bodyStyle.marginTop) + parseInt(bodyStyle.marginBottom));
var bodyWidth = window.innerWidth - (parseInt(bodyStyle.marginLeft) + parseInt(bodyStyle.marginRight));

console.log("Body width = " + window.innerWidth);
//var bodyWidth = window.innerWidth - (parseInt(bodyStyle.marginLeft) + parseInt(bodyStyle.marginRight)
//    + parseInt(bodyStyle.paddingLeft) + parseInt(bodyStyle.paddingRight));

/*
 If Android less than 4.4, we must use window.outerHeight (because it will cut out the soft keyboard space). Otherwise window.innerHeight is what we want to use.
 */

document.body.style.height = bodyHeight + "px";
document.body.style.columnWidth = bodyWidth + "px";
document.body.style.WebkitColumnWidth = bodyWidth + "px";
console.log("Set column width to " + bodyWidth + "px, body height to " + bodyHeight + "px");

window.addEventListener("load", function() {
    console.log("epub-paginate: body width is now: " + document.body.innerWidth);
    var lastElement = document.body.lastElementChild;
    var boundingRect = lastElement.getBoundingClientRect();
    console.log("last element coordinates: " + boundingRect.left + ", " + boundingRect.top);
}, false);

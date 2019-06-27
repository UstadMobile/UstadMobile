/**
 * Ustad Mobile EPUB javascript helper methods
 */
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

    /*
     Inline elements are technically on the baseline. Thus other elements leave the distance
     between the baseline and the descender bottom (e.g. bottom of letter g) blank. Therefor
     we must set the image vertical-align to bottom to avoid extra space creating a blank
     column.
    */
    styleEl.sheet.insertRule("img { max-height: " + (bodyHeight - 0) + "px !important; vertical-align: bottom}");
    console.log("set img max height = " + bodyHeight + "-0 vertical-align: bottom");


    window.addEventListener("load", function() {
        console.log("epub-paginate: body width is now: " + document.body.innerWidth);
        var lastElement = document.body.lastElementChild;
        var boundingRect = lastElement.getBoundingClientRect();
        console.log("last element coordinates: " + boundingRect.left + ", " + boundingRect.top);
    }, false);


    /*
     * For some reason using 'vh' units do not work with the WebView even though they work in the
     * browser. We therefor need to convert those into raw values in pixels using Javscript.
     * African Storybook Project uses vh for font-size to make the font increase with the height of
     * the browser.
     */
    mod.calculateOutViewportUnits = function(sheet) {
        console.log("calculating out viewports for element: " + sheet);
        var ruleName;
        var ruleValue;
        var regex = /((\d+)((\.\d+)?))\s*(vh)/;
        for(var i = 0; i < sheet.cssRules.length; i++) {
           if(sheet.cssRules[i].type === 1) { //1 = CSSStyleRule
               for(var j = 0; j < sheet.cssRules[i].style.length; j++) {
                   ruleName = sheet.cssRules[i].style[j];
                   ruleValue = sheet.cssRules[i].style[ruleName];

                   if(ruleValue.indexOf("vh") !== -1) {
                       var match = regex.exec(ruleValue);
                       if(match) {
                           var vhQty = parseFloat(match[1]);
                           var pxQty = (vhQty / 100) * bodyHeight;
                           var newVal = ruleValue.replace(regex, pxQty+"px");
                           sheet.cssRules[i].style[ruleName] = ruleValue.replace(regex, pxQty+"px");
                       }
                   }
               }
           }
        }
    };


    mod.updateStylesheets = function() {
        for(var i = 0; i < document.styleSheets.length; i++) {
            mod.calculateOutViewportUnits(document.styleSheets[i]);
        }
    };

    window.addEventListener("load", function() {
        mod.updateStylesheets();
    }, false);


    return mod;
}());

/*
 * Pause all video and audio that might be playing
 */
var _ustadMobilePauseAllMedia = (function() {
    return {
        pauseAll: function() {
            console.log("UstadMobile pause all : pause all active media");
            var _mediaElements = document.querySelectorAll("audio, video");
            for(var i = 0; i < _mediaElements.length; i++) {
                if(_mediaElements[i].currentTime > 0 && _mediaElements[i].paused === false
                        && _mediaElements[i].ended === false) {
                    _mediaElements[i].pause();
                }
            }
        }
    }
});

var _ustadMobileAutoplayJs = (function() {
    /*
     * This is not currently supported; but is likely to be re-introduced soon.
     It can be used like so:

     if(document.readyState === "interactive"  || document.readyState === "complete") {
         console.log("umAutoplayFn: document ready");
         _umAutoplayFn();
     }else {
         console.log("umAutoplayFn: document not ready yet: add event listener");
         document.addEventListener("DOMContentLoaded", _umAutoplayFn, false);
     }

     */
    return {
        autoplay: function() {
            var _mediaElements = document.querySelectorAll("audio[data-autoplay]");
            console.log("umAutoplayFn: found " + _mediaElements.length + " media elements");
            for(var _i = 0; _i < _mediaElements.length; _i++) {
                if(_mediaElements[_i].paused === true && _mediaElements[_i].currentTime === 0 && _mediaElements[_i].readyState >= 2) {
                    try {
                        _mediaElements[_i].play();
                    }catch(err) {
                        console.log("error playing " + _mediaElements[_i] + " : " + err);
                    }
                }else if(_mediaElements[_i].seekable.length > 0 && _mediaElements[_i].readyState >= 2){
                    try {
                        _mediaElements[_i].pause();
                        var seekedItFn = function() {
                            _mediaElements[_i].play();
                            _mediaElements[_i].removeEventListener("seeked", seekedItFn, true);
                            mediaEl = null;
                        };

                        _mediaElements[_i].addEventListener("seeked", seekedItFn, true);

                        _mediaElements[_i].currentTime = 0;
                        _mediaElements[_i].play();
                    }catch(err2) {
                        console.log("error playing " + _mediaElements[_i] + " : " + err2);
                    }
                }else {
                    console.log("umAutoplayFn: need to call load");
                    var playItFunction = function(evt) {
                        var myMediaEl = evt.target;
                        try {
                            myMediaEl.play();
                        }catch(err3) {
                            console.log("Exception attempting to play " + myMediaEl.src
                                    + ":" + err3);
                        }

                        myMediaEl.removeEventListener("canplay", playItFunction, true);
                        myMediaEl = null;
                    };
                    _mediaElements[_i].addEventListener("canplay", playItFunction);
                    _mediaElements[_i].load();
                }
            }
        }
    }
});



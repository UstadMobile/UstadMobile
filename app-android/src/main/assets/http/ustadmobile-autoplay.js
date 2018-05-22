var _umAutoplayFn = function() {
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
};

if(document.readyState === "interactive"  || document.readyState === "complete") {
    console.log("umAutoplayFn: document ready");
    _umAutoplayFn();
}else {
    console.log("umAutoplayFn: document not ready yet: add event listener");
    document.addEventListener("DOMContentLoaded", _umAutoplayFn, false);
}

var _mediaElements = document.getElementsByTagName("audio");
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
        var playItFunction = function(evt) {
            var myMediaEl = evt.target;
            try {
                my_mediaElements[_i].play();
            }catch(err3) {
                console.log("Exception attempting to play " + my_mediaElements[_i].src
                        + ":" + err3);
            }
            
            my_mediaElements[_i].removeEventListener("canplay", playItFunction, true);
            myMediaEl = null;
            UstadMobileUtils.runCallback(onPlayCallback, [true], mediaEl);
            onPlayCallback = null;
        };
        _mediaElements[_i].addEventListener("canplay", playItFunction);
        _mediaElements[_i].load();
    }
}
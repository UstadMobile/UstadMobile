/*
 * Android: Pause all video and audio that might be playing
 */
console.log("UstadMobile pause all : pause all active media");
var _mediaElements = document.querySelectorAll("audio, video");
for(var i = 0; i < _mediaElements.length; i++) {
    if(_mediaElements[i].currentTime > 0 && _mediaElements[i].paused === false
            && _mediaElements[i].ended === false) {
        _mediaElements[i].pause();
    }
}




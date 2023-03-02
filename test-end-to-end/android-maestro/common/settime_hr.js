const d = new Date();
var hour = d.getHours();

if (hour < 13) {
  var hr = hour;
} else {
  var hr = hour - 12;
}
if(hr<10){
hr= '0'+hr
}

output.settime_hr = {
    result: hr
}
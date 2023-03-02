const d = new Date();
var minutes = d.getMinutes()+2;

if(minutes<10){
minutes ='0'+minutes
}

output.settime_min = {
    result: minutes
}
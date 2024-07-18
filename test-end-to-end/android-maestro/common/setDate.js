const today = new Date();
const day = today.getDate().toString().padStart(2, '0');
const month = (today.getMonth() + 1).toString().padStart(2, '0'); // Months are zero-indexed
const year = today.getFullYear();
const currentDate = `${day}/${month}/${year}`; // Format: DD/MM/YYYY

const yesterday = (today.getDate() - 1).toString().padStart(2, '0');
const yesterdayDate=`${yesterday}/${month}/${year}`; // Format: DD/MM/YYYY

const tomorrow = (today.getDate() + 1).toString().padStart(2, '0');
const tomorrowDate=`${tomorrow}/${month}/${year}`; // Format: DD/MM/YYYY

const hours = String(today.getHours()).padStart(2, '0');
const minutes = String(today.getMinutes()).padStart(2, '0');
const min_3 = String(today.getMinutes()+3).padStart(2, '0');
const delay1Hr= String(today.getHours()-1).padStart(2, '0');
const time = `${hours}:${minutes}`;
const testTime = `${hours}:${min_3}`;
const delayTime = `${delay1Hr}:${minutes}`;

output.yesterday_Date = yesterdayDate;
output.tomorrow_Date = tomorrowDate;
output.delay_1hr = delayTime;
output.todayDate = currentDate;
output.Time = time;
output.test_Time = testTime;

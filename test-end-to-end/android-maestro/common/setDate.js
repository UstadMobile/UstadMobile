const today = new Date();
const day = today.getDate().toString().padStart(2, '0');
const month = (today.getMonth() + 1).toString().padStart(2, '0'); // Months are zero-indexed
const year = today.getFullYear();
const currentDate = `${day}/${month}/${year}`; // Format: DD/MM/YYYY

const yesterday = (today.getDate() - 1).toString().padStart(2, '0');
const yesterdayDate=`${yesterday}/${month}/${year}`; // Format: DD/MM/YYYY

const hours = String(today.getHours()).padStart(2, '0');
const minutes = String(today.getMinutes()+3).padStart(2, '0');
const delay1Hr= String(today.getHours()+1).padStart(2, '0');
const time = `${hours}:${minutes}`;
const delayTime = `${delay1Hr}:${minutes}`;

output.yesterday_Date = yesterdayDate;
output.after1HrTime = delayTime;
output.todayDate = currentDate;
output.todayTime = time;

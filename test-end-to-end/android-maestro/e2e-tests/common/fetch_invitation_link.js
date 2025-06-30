var maxAttempts = 4;
var urlRegex = /(https?:\/\/\S+)/i;

var email = EMAIL;
var serverUrl = output.SERVER_URL;

console.log("Using EMAIL: " + email);
console.log("Using SERVER_URL: " + serverUrl);

var i = 0;
while (i < maxAttempts) {
    try {
        var response = http.get(serverUrl + "api/testemail/list?to=" + email);
        var emailDataArray = JSON.parse(response.body);

        if (!emailDataArray || emailDataArray.length === 0) {
            console.log("WARNING: No emails found. Attempt: " + i);
            i++;
            continue;
        }

        var emailData = emailDataArray[emailDataArray.length - 1];
        var match = emailData.text.match(urlRegex);

        if (match && match[0]) {
            output.parental_consent_invite = match[0];  // Set value to output
            break;
        } else {
            console.log("WARNING: No valid link found. Attempt: " + i);
        }
    } catch (err) {
        console.log("FAIL: attempt " + i + " failed: " + err);
    }

    i++;
}

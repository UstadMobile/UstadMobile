const maxAttempts = 4;
const email = EMAIL ;
const serverUrl = output.SERVER_URL;

console.log("Using EMAIL: " + email);
console.log("Using SERVER_URL: " + serverUrl);

const urlRegex = /(https?:\/\/\S+)/i; // Regex to find URLs

for (var i = 0; i < maxAttempts; i++) {
    try {
        var response = http.get(serverUrl + "api/testemail/list?to=" + email);
        console.log("Response body:", response.body);

        var emailDataArray = JSON.parse(response.body); // Parse JSON response

        if (!Array.isArray(emailDataArray) || emailDataArray.length === 0) {
            console.log("WARNING: No emails found. Attempt:", i);
            continue;
        }

        // Assuming you need the latest email
        var emailData = emailDataArray[emailDataArray.length - 1];

        // Extract URL using regex
        var match = emailData.text.match(urlRegex);
        if (match && match[0]) {
            console.log("SUCCESS (attempt " + i + "): " + match[0]);

   // Open the link to trigger the app
              if (typeof window !== "undefined") {
                  window.location.href = match[0];  // Open in the current tab
              } else {
                  console.log("Cannot open automatically", match[0]);
              }


            break;
        } else {
            console.log("WARNING: No valid invitation link found. Attempt:", i);
        }
    } catch (err) {
        console.log("FAIL: attempt " + i + " failed: " + err);
    }

    if (i === maxAttempts - 1) {
        throw new Error("Failed after max attempts.");
    }
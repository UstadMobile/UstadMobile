const maxAttempts = 4;

for (var i = 0; i < maxAttempts; i++) {
    try {
        const url = output.SERVER_URL + "api/generate-xapi-statements/runtest?contentTitle=" + TESTFILENAME + "&username=" + USERNAME;
        const rawResponse = http.get(url);
        const response = JSON.parse(rawResponse.body);

        if (response.message === "Successfully generated test statements") {
            console.log("generate_content_usage_data_via_http.js: SUCCESS : attempt" + i);
            break;
        } else {
            throw new Error("Unexpected message:" + response.message);
        }

    } catch (err) {
        console.log("generate_content_usage_data_via_http.js: FAIL : attempt" + i + "failed:" + err);
        if (i === maxAttempts - 1) {
            throw err;
        }
    }
}

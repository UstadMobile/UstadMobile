# LibIHttp

LibIHttp (e.g. Interface Http) makes it easier to use common code logic to process http requests 
which may come from different underlying implementations e.g. Ktor, NanoHTTPD, OKHttp, Fetch (JS) etc.
This can be needed for logic that handles API implementation (e.g. xAPI, OneRoster, etc) which is 
used both in the embedded server (Android, Desktop) and the main server (KTOR).

The interfaces (IHttpHeaders, IHttpResponse, IHttpRequest) wrap various implementations (Ktor, Okhttp,
JS, etc). This is done 'cheaply' (e.g. without copying  the entire request/response), adapters are used.


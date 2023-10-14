# Coupon Protocol

A protocol to save on http bandwidth and cache storage.

1) Avoid downloading a response body when the response data is available locally, even if data was 
   originally fetched from a different URL

Client Request:

```
GET /path/welcome.mp4

Hostname: server.com
Cache-Control: cache 
Coupon-Expect-Sha-256: ...
Coupon-Expect-Content-Type: video/mp4;
Coupon-Expect-Size: ...
```

Coupon-Expect-Sha-256 is used to match the response body, even where the URL might have been different.
Coupon-Expect-Content-Type: where a matching response body is found, the cache needs to know the content-type
that should be served.
Coupon-Expect-Content-Length (optional): indicate the expected size. This can be used by the cache
to decide whether or not to attempt to contact other local nodes.


Cache response:

```
Cache: Hit
Coupon-Actual-Sha-256: ...
Content-Type: video/mp4

```

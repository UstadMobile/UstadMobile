# Coupon Protocol

A protocol to save on http bandwidth and cache storage.

Objectives:

1) Avoid downloading (and storing) the same http body again even when the URL is different (happens 
   when the same content e.g. Javascript Library, video, etc is downloaded via multiple different 
   URLs).

2) Enable devices on a local network to form a distributed cache without the need for any central 
   server.

3) Enable network administrators to advertise a shared cache that clients can opt-in to use for 
   requests that do not contain sensitive information. This effectively allows network administrators 
   to independently create an opt-in CDN to reduce bandwidth usage.

How:

When the client knows the expected checksum, the response body can be independently verified. This
makes it possible to retrieve the response body from an untrusted source (e.g. peer node or local
network cache) safely. 

Developers can use this functionality for requests that do not contain personal or sensitive 
information (whilst continuing to route requests that contain sensitive, typically much smaller,
requests directly). The vast majority of network traffic is used to deliver media assets (e.g. video,
Javascript libraries, CSS, images, etc) that do not contain personal or sensitive information.

There is some privacy trade-off (e.g. a the operator of a local network
cache could distinguish between https urls, whereas they would otherwise only be able to see the
hostname being connected to). 

## Requests

Client Request:

```
GET /hello/welcome.mp4

Hostname: server.com
Cache-Control: cache 
Coupon-Expect-Sha-256: ...
Coupon-Consistent-Url: /assets/welcome.mp4
Coupon-Expect-Content-Type: video/mp4;
Coupon-Expect-Size: ...
```

Coupon-Expect-Sha-256 is used to match the response body, even where the URL might have been 
different. 
Coupon-Expect-Content-Type: where a matching response body is found, the cache needs to know the 
content-type should be served in the reply.
Coupon-Expect-Content-Length (optional): indicate the expected size. This can be used by the cache
to decide whether or not to attempt to contact other local nodes.

Cache response:

```
Cache: Hit
Coupon-Actual-Sha-256: ...
Content-Type: video/mp4
```

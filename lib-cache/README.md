
# Lib-Cache (Coupon)

This is a bandwidth efficient cache that: 

* Provide reliable offline storage: it is possible to add a retention lock to prevent specific 
  urls from being evicted from the cache. E.g. when a user selects an item for use offline usage, 
  the related urls should be retained in the cache unless/until the user changes their mind.

* Enables programmatic storage: data can be stored in the cache without actually downloading it 
  from the network. E.g. if a user adds a photo in an app offline, we might already know what the
  url is going to be once it is uploaded (e.g. server.com/profile/photo.jpg ). The data can be
  pre-cached so that other components of the app can use the data (eg photo) without waiting for 
  upload.

* Enable distributed caching:  neighbors can be discovered on the Local Area Network via DNS-SD (Bonjour)
  service discovery. When there are many devices on the same network that want to download the 
  same file, they can download from each other. This can dramatically reduce bandwidth usage 
  where multiple nearby devices need the same files (e.g. multiple tablets in a classroom).

* Allows opt-in shared caching: where a shared cache (e.g. Squid, Traffic Control, etc) is present
  it can be used for requests that do not include personal or sensitive information.

* Includes an OKHttp interceptor that can be used instead of the built-in (final and non-extensible)
  cache that is included with OKHttp. This enables the cache to be used with any network library
  that supports OkHttp (including Ktor, Android image loaders like COIL, etc.)

Handling checksums:

[Subresource Integrity](https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity)
headers are used to ensure that data fetched from another node or shared cache has not been tampered
with. The ideal setup is to use the integrity (SHA-256) checksum as the etag, thus creating a 
consistent etag. Only SHA-256 is supported for now.

This works as follows:

Response:

The response from the cache will include the sha256 integrity. If no other etag is set, the sha256
checksum will be returned as the etag as follows:
```
Etag: sha256-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC
X-Etag-Is-Integrity: true
```
If the etag is something else, then the header can be separate:

```
Etag: something-else
X-Integrity: sha256-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC
```

Request:
In order to make a request where the integrity is known, include it directly or reference a url
that has the checksum

```
X-Accept-Integrity: sha256-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC
```
OR
```
X-Accept-Integrity: https://originserver.com/dir/file.sha256sum
```

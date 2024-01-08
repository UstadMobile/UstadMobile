
# Lib-Cache (Coupon)

This is a bandwidth efficient cache that aims to minimize bandwidth and storage usage.

This is done through:

* Retention locks: it is possible to add a programmatic lock to prevent data from being
  evicted from the cache. E.g. when a user selects an item for use offline usage, the related
  items should be retained in the cache until the user changes their mind.
* Distributed: neighbors can be discovered on the Local Area Network via DNS-SD (Bonjour)
  service discovery. When there are many devices on the same network that want to download the 
  same file, they can download from each other. This can dramatically reduce bandwidth usage 
  where multiple nearby devices need the same files (e.g. multiple tablets in a classroom).
* Opt-in shared caching: where a shared cache (e.g. Squid, Traffic Control, etc) is present
  it can be used for requests that do not include personal or sensitive information.


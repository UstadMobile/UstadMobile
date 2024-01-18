# Package com.ustadmobile.core.domain.blob

The blob system handles storing and retrieving binary data such as content (e.g. video, epubs, xAPI,
etc.), profile pictures, assignment file submissions, etc. A blob is given a fixed url based on its
sha256 sum e.g. http(s)://endpoint.com/api/blob/(sha256-sum).

Blob urls faciltiate offline access to data. The cache (as implemented in lib-cache) both
caches data (as a "normal" cache would) and allows cache entries to be marked as to be retained 
indefinitely (eg. where a user has selected those items for offline use).

## HTTP API

### POST /api/blob/upload-init-batch (start a batch upload)

Initiates a blob batch upload with a list of blobs that the client intends to upload. The server will
reply with a list of those blobs that it does not yet have (allowing the client to skip those already
on the server). The response also includes info on any partial upload progress (if any) so the client
can resume the upload from the next required chunk. The server will create a UUID to identify each 
upload.

### POST /api/blob/upload-batch-data (upload blob data in chunks)

Data is uploaded in chunks (via ChunkedUploadUseCase). Each request contains the batch uuid and 
upload uuid. The server appends data as it is received in chunks. The last chunk includes http 
headers.

### POST /api/blob/upload-item

Data is uploaded in chunks for a single item without any batch. The HTTP endpoint will run the SHA256,
store the data under the appropriate blob URL, and then return the URL used. This endpoint is used by
the Javascript client.

### GET /api/blob/sha256 (Retrieve the blob itself)

The blob data is accessed via a normal http request. Partial http requests are supported.

Platforms that provide offline functionality (Android, Desktop) use lib-cache to store and retrieve
blobs. Blobs always have the cache-control immutable header because their URL is a function of the
checksum, and hence never changes.

Lib-cache can be instructed to retain any given url indefinitely (e.g. where a user indicates that 
they want to keep an item available for offline use). If/when a blob is no longer required, the 
retention lock is removed, which allows for the file to be evicted from the cache.

## Domain (UseCase) API

Client:

* SaveLocalUrisAsBlobsUseCase : saves data from local URI(s) as blob(s). 
** Android/Desktop: SaveLocalUrisAsBlobsUseCase will run an SHA256 checksum and store the blob in its 
   own local cache as /api/blob/sha256. It will then use BlobUploadClientUseCase to upload the blobs
   saved to the upstream server. BlobUploadClientUseCase uses ChunkedUploadClientUseCase to upload
   each blob (in chunks) to the server.

** Web: SaveLocalUrisAsBlobsUseCase will directly upload to an endpoint on the server. The server 
   endpoint will run the SHA-256 checksum and return the URL in which the blob is stored.

* BlobUploadClientUseCase: used only on JVM and Android. Requests an upload session from the server,
  and then uploads all the blobs that the server does not already have. Also supports resuming 
  uploads from the last chunk received. This is used by SaveLocalUrisAsBlobsUseCase to upload blobs
  after they have been saved into the local cache, and also by ContentImporters to upload blobs after
  they have been stored for content.

Server:

* BlobUploadServerUseCase: manages an http server endpoint to receive blob uploads. This should be
  used (retained) on the server side as a singleton.

# Package com.ustadmobile.core.contentformats

Each package contains support for a given format e.g. EPUB. H5P, xAPI, etc. This often includes 
models (e.g. for use with Kotlinx Serialization) that are used by both the importer and view. There 
is a ContentImporter implementation for each format on supported platforms.

# Package com.ustadmobile.core.domain.blob.saveandmanifest

These UseCases address a common requirement when importing content: an asset needs to be saved as a 
blob and have a corresponding CacheEntryManifest (where the bodyDataUrl links to the blobUrl). 

These include adding all assets from a directory, zip, string, etc

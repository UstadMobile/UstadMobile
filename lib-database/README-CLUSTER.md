Cluster:

Objective: Distribute all changes to an SQLite database to all nearby devices. This is intended for
situations where there is more than one device but not so many devices that a local server is 
required and worth the maintenance overhead.

Cluster creation and joining:

* On any device: a public/private keypair (shared within the cluster) with a password is generated
* Joining: on any other device can join by obtaining the public/private keypair and the password
* Any node (e.g. tablet with SIM card) may be set to relay changes from other nodes to the online 
  server.

Operation:

* Each node discovers other nodes (via Bluetooth Low Energy advertising and/or Network Service Discovery)
* Local DB (SQLite) changes detected (via SQL trigger): When any transaction occurs that results in 
  changes to tables, the change is given a Transaction ID (64bit unique "snowflake") ID and the JSON 
  of all entities changed is saved to disk (encrypted using the shared key, compressed using Gzip).
* The node will attempt to send the change to all other nodes that are members of the cluster that
  are reachable at the time.
* The node will periodically poll all other nodes to request all the change IDs it knows about since 
  the last time it was polled. This will enable the node to discover any changes that happened on other
  nodes when the given nodes were not reachable (e.g. both nodes powered on, bluetooth/wifi enabled, 
  etc - does not require Internet connectivity) at the same time. It will then request any changes 
  it did not yet receive from the same node.

Actioning changes:

* All rows (on all tables) have a last-modified timestamp. Updates will only be executed where the 
  last-modified timestamp is greater. When nodes interact, they will ALWAYS compare system clock
  times. If the system clocks are too far out of sync, then the interaction is aborted.
* All data transmission uses the shared public/private key




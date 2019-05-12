/**
 * Ustad Mobile has peer to peer functionality for zero configuration downloading of courses from
 * nearby devices.
 *
 * There are two scenarios for local downloads:
 *
 * - The two nodes are on the same wifi network (using Network Service Discovery)
 * - The two nodes are not on the same wifi network but are within range of each other (using wifi
 *  direct (p2p) and bluetooth)
 *
 * Same wifi network:
 *  Each node advertises itself network service discovery. The service name is the device name, the
 *  service type comes from buildconfig properties appNetworkServiceType  (e.g. _ustad). The full
 *  service type would then be _ustad._tcp . Network Service Discovery provides the port number.
 *
 * Different wifi network:
 *  Each node advertises a wifi direct service. The wifi direct instance name and service type are
 *  fixed in line with recommended practice as per the wifi p2p spec. When a service discovery
 *  request is made any node nearby with the requested service available should respond.
 *
 *  All nodes have the same instance name and service type. The wifi p2p system can discover the name
 *  of the node via it's peers list. The instance name is set in buildconfig.properties using the
 *  appP2pServiceInstanceName property.
 *
 *  The service includes the bluetooth mac address. The initiating node connects using bluetooth to
 *  the other node to query if entries are available on that node, and to request a wifi direct group
 *  connection using the no prompt setup as per the WifiBuddy library.
 *
 */
package com.ustadmobile.port.sharedse.networkmanager;



# Financial Distributed System - Snapshots

## Overview

This project represents an example of a [distributed system](https://www.geeksforgeeks.org/what-is-a-distributed-system/) where all nodes belong to a financial network. Each node in the system holds a fixed initial amount of small coins (bitcake). The system allows for a TRANSACTION message to transfer any amount of bitcakes to another node.

A **snapshot** is a functionality that collects the current state of bitcakes in the system. Since the system is active and bitcakes are continuously being sent, the result of the snapshot contains data on how many bitcakes each node has, as well as how many are in transit between nodes.

The [distributed system](https://www.geeksforgeeks.org/what-is-a-distributed-system/) is implemented in [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) and utilizes completely asynchronous [non-FIFO](https://serialsjournals.com/abstract/80300_37-avinash_yadlapat.pdf) communication between nodes. Each node has a defined number of neighbors for direct communication. If a node needs to communicate with a non-direct neighbor, it must find an intermediary.

## Features

-   Each node maintains a specific number of bitcakes.
-   The configuration for each node includes an attribute "initiators," which lists nodes that can initiate snapshots.
-   Snapshots can be created from any node designated as an initiator.
-   The initiator node supports:
    -   Initiating multiple snapshots simultaneously, but only one snapshot can be processed at a time from the same node, using a variation of the [Lai-Yang algorithm](https://homepage.cs.uiowa.edu/~ghosh/10-16-03.pdf).
    -   Concurrent collection of snapshots from different initiator nodes using the [Spezialetti-Kearns](https://ics.uci.edu/~cs237/reading/files/An%20introduction%20to%20snapshot%20algorithms%20in%20distributed%20computing.pdf) algorithm.

All nodes, whether initiators or regular nodes, frequently exchange their bitcake supplies based on user commands. The snapshot result represents the current state of bitcakes in the distributed system.

## Communication

-   Nodes can run on the same machine, listening on different ports, and are bound to localhost.
-   Node A can send a message to Node B if:
    -   Nodes A and B are direct neighbors according to the system configuration.
    -   Node B is an initiator of the snapshot algorithm in a neighboring region, and Node A has received a message containing Node B's ID indirectly.

The system supports scripting to launch multiple nodes simultaneously, with commands for each node read from their respective text files.

## Functional Requirements

-   Only designated initiator nodes can start snapshots.
-   If a user requests a snapshot from a non-initiator node, the system logs an error and continues operation.
-   If a snapshot is requested from an initiator node that has not completed its previous snapshot, an error is logged, and the system continues normally.

All issues are handled gracefully to ensure normal operation. When an initiator node repeats a snapshot, the process is executed differentially, starting a "new" history for the initiator node after collecting the snapshot (Li algorithm).

During the collection of a snapshot, the system exchanges a total of O(e) messages (where e is the number of connections between nodes), even when multiple concurrent snapshots are running (Spezialetti-Kearns algorithm).

## Non-Functional Requirements

-   **Error Handling**:
    
    1.  When a user requests a snapshot from a non-initiator node, the system logs an error on the console and continues to operate normally.
    2.  If a snapshot is requested from an initiator node that has not completed its previous snapshot, the system logs an error on the console and continues to operate normally.
-   **Graceful Degradation**: All issues are resolved gracefully to ensure the system continues to function normally.
    
-   **Differential Snapshotting**: When an initiator node repeats a snapshot, the results are recorded differentially, starting a "new" history for the initiator node (Li algorithm).
    
-   **Message Complexity**: During the collection of a snapshot, the system exchanges a total of O(e) messages (where e is the number of connections between nodes), even with multiple concurrent snapshots.
    

## Configuration

Each node's configuration file specifies:

-   The number of nodes in the distributed system.
-   The port on which each node listens.
-   The list of neighbors for each node.
-   The list of nodes that can initiate snapshots.

Terminal messages provide information about which node is the parent of the current node in the spanning tree formed during the collection of results. If the node is an initiator of the snapshot, it displays the data collected in each round of result exchange.

## Getting Started

To get started, clone the repository and follow the instructions in the configuration files to set up and run your nodes.
```
git clone https://github.com/lmicovic/financial-distributed-network-snapshot.git
cd <repository-directory>
# Follow the setup instructions in the documentation
```

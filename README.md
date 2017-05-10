# Snapshot
Implementation of Chandy Lamport Global Snapshot protocol

## Project Description

### Part 1

Implement a distributed system consisting of *n* nodes, numbered *0 to n − 1*, arranged in a certain topology. The topology and information about other parameters will be provided in a configuration file.

All channels in the system are bidirectional, reliable and satisfy the first-in-first-out (FIFO) property.  For each channel, the socket connection should be created at the beginning of the program and should stay intact until the end of the program. All messages between neighboring nodes are exchanged over these connections.

#### All nodes execute the following protocol:

- Initially, each node in the system is either ***active*** or ***passive***. At least one node must be active at the beginning of the protocol.

- While a node is active, it sends anywhere from ***minPerActive*** to ***maxPerActive*** messages, and then turns passive. For each message, it makes a uniformly random selection of one of its neighbors as the destination. Also, if the node stays active after sending a message, then it waits for at least ***minSendDelay*** time units before sending the next message.

- Only an active node can send a message.

- A passive node, on receiving a message, becomes active if it has sent fewer than ***maxNumber*** messages (summed over all active intervals). Otherwise, it stays passive. We refer to the protocol described above as the **MAP protocol**.

### Part 2

Implement the **Chandy and Lamport’s** protocol for recording a consistent global snapshot. Assume that the snapshot protocol is always initiated by node 0 and all channels in the topology are bidirectional. Use the snapshot protocol to detect the termination of the MAP protocol described in Part 1. The MAP protocol terminates when all nodes are passive and all channels are empty. To detect termination of the MAP protocol, augment the Chandy and Lamport’s snapshot protocol to collect the information recorded at each node at node 0 using a converge-cast operation over a spanning tree.

Note that, in this project, the messages exchanged by the MAP protocol are application messages and the messages exchanged by the snapshot protocol are control messages. The rules of the MAP protocol (described in Part 1) only apply to application messages. They do not apply to control messages.

### Part 3

To test the implementation of Chandy and Lamport’s snapshot protocol is correct, implement **Fidge/Mattern’s vector clock protocol**. The vector clock of a node is part of the local state of the node and its value is also recorded whenever a node records its local state. Node 0, on receiving the information recorded by all the nodes, uses these vector timestamps to verify that the snapshot is indeed consistent. Note that only application messages will carry vector timestamps.

The vector clock concurrency is checked using a python script. The script performs pair wise vector clock concurrency check. At the end of the execution the result is presented to the user.

Use the following syntax to run the concurrency checker

`python Concurrency.py <number_of_nodes> <path/to/configuration/file.txt>`

## Format of the configuration file

The configuration file will be a plain-text formatted file no more than *100kB* in size. Only lines which begin with an unsigned integer are considered to be valid. Lines which are not valid should be ignored. The configuration file will contain *2n + 1* valid lines. The first valid line of the configuration file contains SIX tokens. 

The *first token* is the number of nodes in the system. 
The *second* and *third* tokens are values of minPerActive, and maxPerActive respectively. 
The *fourth* and *fifth* tokens are values of minSendDelay and snapshotDelay, in milliseconds. 
The *sixth* token is the value of maxNumber. 

After the first valid line, the next *n* lines consist of three tokens. 

The first token is the node ID. The second token is the host-name of the machine on which the node runs. The third token is the port on which the node listens for incoming connections. After the first *n + 1* valid lines, the next *n* lines consist of a space delimited list of at most *n − 1* tokens.

The *k* th valid line after the first line is a space delimited list of node IDs which are the neighbor of node *k*. The parser is robust concerning leading and trailing white space or extra lines at the beginning or end of file, as well as interleaved with valid lines. The *'#’* character will denote a comment. On any valid line, any characters after a *'#'* character should be ignored.

## Launching the application

‘launcher.sh’ will launch multiple instances of the application as nodes in the machine that you have entered in the configuration file. 

Run startup using the following syntax

`sh startup.sh <path/to/configuration/file.txt>`

## Terminating the application

Once the nodes have taken the required number of snapshots as mentioned in the configuration file, each node will start it’s execution. But if the application is stalled due to some failures, it is better to kill the processes. ‘cleanup’  is the script to use while logs in to appropriate machines to kill the processes launched by the ‘launcher’ script. 

Run cleanup using the following syntax

`sh cleanup.sh <path/to/configuration/file.txt>`

***Note:*** 
1. Both launcher and cleanup script depend on the correct configuration file to run.
2. All the python files should be placed in the same directory with the configuration file

## Output Format

If the configuration file is named `<config_name>.txt` and is configured to use n nodes, then your program should output n output files, named in according to the following format:

`<config_name>-<node_id>.out, where node_id ∈ {0, ..., n − 1}.`

The output file for process *j* should be named `<config_name>-j.out` and should contain the following: 

If your program took m snapshots, then each output file should contain *m* lines. The *i* th line should contain the vector time stamp of the *i* th snapshot as seen by process *j*. Each line of the output file should contain *n* space delimited tokens, each of which should be a non-negative integer. In each line, the timestamps must appear in increasing order of process id. That is, the *k th* number in the *i th* line should be the time stamp value for process *k*. 

## Supported Environments

The program is built, executed and tested on CentOS and Ubuntu machines. It won’t run for Windows and Mac Machines (though porting them to other operating system is trivial).    

## Licence 

This project is licensed under the MIT License - see the [LICENCE](../master/LICENSE) file for details
 

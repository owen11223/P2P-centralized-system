Setting up vms:
On Chameleon, launch 1 instance named "group5-server",

and 4 instances named "group5-peer".

Use the snapshot image "group5-pa1-ss"

Set the security group to "group5" and network to "group5-net"

Create a keypair and name the key as "gp5-kp" and "gp5-kp.pem"


1. SSH to server/deployment vm
ssh -i .ssh/gp5-kp.pem group5@129.114.111.236


2. then tmux and connect to peers
ssh -i .ssh/gp5-kp.pem group5@192.168.5.15
etc.

3. setup the "peer-host-names" file
e.g. 
	group5@192.168.5.14
	group5@192.168.5.16
	...

4. copy the project folder to each peer using tnb.sh (transfer and build)
	$ bash scripts/tnb.sh

5. setup the files in the peers using fileSetup.sh
	$ bash scripts/fileSetup.sh

6. start the centralserver
	$ java -cp bin pa1.CentralIndexServer
	(choose option 2)
	
7. run the experiments with expOnce.sh
	$ bash scripts/expOnce.sh


# P2P-centralized-system

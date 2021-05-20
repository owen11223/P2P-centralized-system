SSH to machines

1. to server/deployment vm
ssh -i .ssh/gp5-kp.pem group5@129.114.111.236


2. then tmux and connect to peers
ssh -i .ssh/gp5-kp.pem group5@192.168.5.15
etc.

3. pull/clone the freshest clone from gitlab to the server
group5@group5-server:~/$ git clone ...   

4. setup the "peer-host-names" file
e.g. 
	group5@192.168.5.14
	group5@192.168.5.16
	...

5. copy the project folder to each peer using tnb.sh (transfer and build)
	$ bash scripts/tnb.sh

6. setup the files in the peers using fileSetup.sh
	$ bash scripts/fileSetup.sh

7. start the centralserver
	$ java -cp bin pa1.CentralIndexServer
	(choose option 2)
	
8. run the experiments with expOnce.sh
	$ bash scripts/expOnce.sh



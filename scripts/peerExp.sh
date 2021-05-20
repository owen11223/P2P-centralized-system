#!/bin/bash

# experiment parameters
debugFlag=false
fileListfp=files/flist.txt
regfileFlag=false

# arguments
expType=$1
numPeers=$2

pssh -h peer-host-names -P -x "-i ~/.ssh/gp5-kp.pem" -t 0 "
     cd group-5/pa1/;
     . ./peer.config;
     echo $"peerid";
     java -cp bin pa1.Driver $"peerid" $"serverip" $debugFlag $fileListfp $regfileFlag $expType $numPeers;
"

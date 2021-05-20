#!/bin/bash


# debugFlag=true
# fileListfp=files/flist.txt
# regfileFlag=false
# expType=$1
# numPeers=$2

pssh -h peer-host-names -P -x "-i ~/.ssh/gp5-kp.pem" -t 0 "
     cd group-5/pa1/;
     . ./peer.config;
     echo $"peerid";
     java -cp bin pa1.TestProg $"peerid";
"

#!/bin/bash

cd ~/group-5/pa1/
fileListfp=files/flist.txt
regfileFlag=false

. ./peer.config

java -cp bin pa1.Driver $peerid $serverip true $fileListfp  

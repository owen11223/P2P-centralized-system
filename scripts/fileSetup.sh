#!/bin/bash

echo "**Starting peer file setup**"

pssh -h "peer-host-names" -x "-i ~/.ssh/gp5-kp.pem" -t 0 -I < scripts/peerFileSetup.sh

echo "**checking the number of files created (101010  101010 1400880)**"
pssh -h "peer-host-names" -P -x "-i ~/.ssh/gp5-kp.pem" -t 0 "cd group-5/pa1/files/arch; pwd; ls | wc"

echo "**check filenames for each peer**"
pssh -h "peer-host-names" -P -x "-i ~/.ssh/gp5-kp.pem" -t 0  "head group-5/pa1/files/flist.txt"

echo "**peer file setup done**"
